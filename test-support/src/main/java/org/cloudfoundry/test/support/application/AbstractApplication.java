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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.RestageApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.util.DelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.cloudfoundry.util.tuple.TupleUtils.function;

abstract class AbstractApplication implements Application {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String buildpack;

    private final CloudFoundryOperations cloudFoundryOperations;

    private final Mono<String> host;

    private final File location;

    private final String name;

    private final AsyncRestOperations restOperations;

    private final Integer memoryMultiplier;

    protected AbstractApplication(String buildpack, CloudFoundryOperations cloudFoundryOperations, File location, String name, AsyncRestOperations restOperations, Integer memoryMultiplier) {
        this.buildpack = buildpack;
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.host = getHost(cloudFoundryOperations, name);
        this.location = location;
        this.name = name;
        this.restOperations = restOperations;
        this.memoryMultiplier = memoryMultiplier;
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
        return getManifest(this.location)
            .then(manifest -> Mono.when(getApplication(this.location, manifest), getMemory(manifest, this.memoryMultiplier)))
            .then(function((application, memory) -> this.cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                    .application(application)
                    .buildpack(this.buildpack)
                    .memory(memory)
                    .name(this.name)
                    .noStart(true)
                    .build())
                .then(getEnvironmentVariables()
                    .flatMap(function((key, value) -> this.cloudFoundryOperations.applications()
                        .setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
                            .name(this.name)
                            .variableName(key)
                            .variableValue(value)
                            .build())), 1)
                    .then())
                .then(this.cloudFoundryOperations.applications()
                    .start(StartApplicationRequest.builder()
                        .name(this.name)
                        .build()))
                .doOnError(t -> this.logger.error("Error pushing {}", this.name, t))
                .doOnSubscribe(s -> this.logger.info("Pushing {}", this.name))));
    }

    @Override
    public final Mono<String> request(String path) {
        return this.host
            .then(host -> Mono.<ResponseEntity<String>>create(emitter -> this.restOperations.getForEntity(String.format("http://%s%s", host, path), String.class)
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
            .retryWhen(DelayUtils.exponentialBackOffError(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofMinutes(1)))
            .map(HttpEntity::getBody);
    }

    @Override
    public final Mono<Void> restage() {
        return this.cloudFoundryOperations.applications()
            .restage(RestageApplicationRequest.builder()
                .name(this.name)
                .build())
            .doOnError(t -> this.logger.error("Error restaging {}", this.name, t))
            .doOnSubscribe(s -> this.logger.info("Restaging {}", this.name));
    }

    private static Mono<Path> getApplication(File location, Map<String, String> manifest) {
        return Mono.just(new File(location, manifest.get("path")).toPath());
    }

    private static Flux<Tuple2<String, String>> getEnvironmentVariables() {
        return Flux.fromIterable(System.getenv().entrySet())
            .filter(entry -> entry.getKey().startsWith("JBP_CONFIG_"))
            .map(entry -> Tuples.of(entry.getKey(), entry.getValue()));
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
    private static Mono<Map<String, String>> getManifest(File location) {
        return Mono
            .fromCallable(() -> OBJECT_MAPPER.readValue(new File(location, "manifest.yml"), Map.class))
            .map(m -> ((Map<String, List<Map<String, String>>>) m).get("applications").get(0));
    }

    private static Mono<Integer> getMemory(Map<String, String> manifest, Integer memoryMultiplier) {
        return Mono.just(manifest)
            .map(m -> m.get("memory"))
            .map(AbstractApplication::resolveMemory)
            .map(m -> m * memoryMultiplier);
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

}
