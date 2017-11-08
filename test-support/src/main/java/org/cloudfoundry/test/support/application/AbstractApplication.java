/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.test.support.application;

import org.cloudfoundry.doppler.LogMessage;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationManifest;
import org.cloudfoundry.operations.applications.ApplicationManifestUtils;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.operations.applications.PushApplicationManifestRequest;
import org.cloudfoundry.operations.applications.RestageApplicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestOperations;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

abstract class AbstractApplication implements Application {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String buildpack;

    private final CloudFoundryOperations cloudFoundryOperations;

    private final Mono<String> host;

    private final File location;

    private final String memory;

    private final String name;

    private final AsyncRestOperations restOperations;

    protected AbstractApplication(String buildpack, CloudFoundryOperations cloudFoundryOperations, File location, String memory, String name, AsyncRestOperations restOperations) {
        this.buildpack = buildpack;
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.host = getHost(cloudFoundryOperations, name);
        this.location = location;
        this.memory = memory;
        this.name = name;
        this.restOperations = restOperations;
    }

    @Override
    public final Mono<Void> delete() {
        return this.cloudFoundryOperations.applications()
            .delete(DeleteApplicationRequest.builder()
                .deleteRoutes(true)
                .name(this.name)
                .build())
            .doOnError(t -> this.logger.error("Error deleting {}", this.name, t))
            .doOnSubscribe(s -> this.logger.info("Deleting {}", this.name));
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Mono<Void> push() {
        return this.cloudFoundryOperations.applications()
            .pushManifest(PushApplicationManifestRequest.builder()
                .manifest(getManifest(this.logger, this.buildpack, this.location, this.memory, this.name))
                .build())
            .doOnError(t -> this.logger.error("Error pushing {}", this.name, t))
            .onErrorResume(this::printRecentLogs)
            .doOnSubscribe(s -> this.logger.info("Pushing {}", this.name));
    }

    @Override
    public final Mono<String> request(String path) {
        return this.host
            .flatMap(host -> Mono.<ResponseEntity<String>>create(emitter -> this.restOperations.getForEntity(String.format("http://%s%s", host, path), String.class)
                .addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

                    @Override
                    public void onFailure(Throwable ex) {
                        emitter.error(ex);
                    }

                    @Override
                    public void onSuccess(ResponseEntity<String> result) {
                        emitter.success(result);
                    }

                })))
            .doOnError(t -> this.logger.warn("Error while making request: {}", t.getMessage()))
            .log("stream.pre.error")
            .onErrorResume(this::printRecentLogs)
            .log("stream.post.error")
            .map(HttpEntity::getBody);
    }

    @Override
    public final Mono<Void> restage() {
        return this.cloudFoundryOperations.applications()
            .restage(RestageApplicationRequest.builder()
                .name(this.name)
                .build())
            .doOnError(t -> this.logger.error("Error restaging {}", this.name, t))
            .onErrorResume(this::printRecentLogs)
            .doOnSubscribe(s -> this.logger.info("Restaging {}", this.name));
    }

    private static Map<String, String> getEnvironmentVariables() {
        return System.getenv().entrySet().stream()
            .filter(entry -> entry.getKey().startsWith("JBP_CONFIG_"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Mono<String> getHost(CloudFoundryOperations cloudFoundryOperations, String name) {
        return cloudFoundryOperations.applications()
            .get(GetApplicationRequest.builder()
                .name(name)
                .build())
            .map(applicationDetail -> applicationDetail.getUrls().get(0))
            .cache();
    }

    @SuppressWarnings("unchecked")
    private static ApplicationManifest getManifest(Logger logger, String buildpack, File location, String memory, String name) {
        ApplicationManifest template = ApplicationManifestUtils.read(new File(location, "manifest.yml").toPath()).stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to find an application definition"));

        ApplicationManifest.Builder builder = ApplicationManifest.builder().from(template)
            .name(name)
            .buildpack(buildpack)
            .putAllEnvironmentVariables(getEnvironmentVariables());

        Optional.ofNullable(memory)
            .map(raw -> {
                logger.warn("Overriding {} memory with {}", name, memory);
                return resolveMemory(raw);
            })
            .ifPresent(builder::memory);

        return builder.build();
    }

    private static Integer resolveMemory(String raw) {
        Character last = raw.charAt(raw.length() - 1);
        if (Character.isDigit(last)) {
            return Integer.parseInt(raw);
        }

        if (Character.toLowerCase(last) == 'g') {
            return Integer.parseInt(raw.substring(0, raw.length() - 1)) * 1024;
        }

        if (Character.toLowerCase(last) == 'm') {
            return Integer.parseInt(raw.substring(0, raw.length() - 1));
        }

        throw new IllegalArgumentException(String.format("Illegal memory size %s", raw));
    }

    private <T> Mono<T> printRecentLogs(Throwable t) {
        return this.cloudFoundryOperations.applications()
            .logs(LogsRequest.builder()
                .name(this.name)
                .recent(true)
                .build())
            .map(LogMessage::getMessage)
            .doOnNext(System.out::println)
            .then(Mono.error(t));
    }

}
