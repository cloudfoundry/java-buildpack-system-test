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
import org.cloudfoundry.util.DelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestOperations;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
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

    protected AbstractApplication(String buildpack, CloudFoundryOperations cloudFoundryOperations, File location, String name, AsyncRestOperations restOperations) {
        this.buildpack = buildpack;
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.host = getHost(cloudFoundryOperations, name);
        this.location = location;
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
            .doOnSubscribe(s -> this.logger.info("Deleting {}", this.name));
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Mono<Void> push() {
        return getManifest(this.location)
            .then(manifest -> Mono.when(getApplication(this.location, manifest), getMemory(manifest)))
            .then(function((application, memory) -> this.cloudFoundryOperations.applications()
                .push(PushApplicationRequest.builder()
                    .application(application)
                    .buildpack(this.buildpack)
                    .memory(memory)
                    .name(this.name)
                    .build())
                .doOnSubscribe(s -> this.logger.info("Pushing {}", this.name))));
    }

    @Override
    public final Mono<String> request(String path) {
        return this.host
            .then(host -> Mono.<ResponseEntity<String>>create(emitter -> this.restOperations.getForEntity(String.format("http://%s%s", host, path), String.class)
                .addCallback(new ListenableFutureCallback<ResponseEntity<String>>() {

                    @Override
                    public void onFailure(Throwable ex) {
                        emitter.fail(ex);
                    }

                    @Override
                    public void onSuccess(ResponseEntity<String> result) {
                        emitter.complete(result);
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
            .doOnSubscribe(s -> this.logger.info("Restaging {}", this.name));
    }


    private static File createZip(File path) throws IOException {
        Path zip = Files.createTempFile("cf-package-", ".zip");
        Files.delete(zip);

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(URI.create("jar:file:" + zip.toString()), Collections.singletonMap("create", "true"))) {
            Path source = Paths.get(path.toURI());
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    String relative = source.relativize(dir).toString();

                    if (!relative.isEmpty()) {
                        Files.createDirectory(zipFileSystem.getPath(relative));
                    }

                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String relative = source.relativize(file).toString();

                    if (!relative.isEmpty()) {
                        Files.copy(file, zipFileSystem.getPath(relative), COPY_ATTRIBUTES);
                    }

                    return CONTINUE;
                }
            });

        }

        return zip.toFile();
    }

    private static Mono<InputStream> getApplication(File location, Map<String, String> manifest) {
        return Mono
            .fromCallable(() -> {
                File path = new File(location, manifest.get("path"));

                if (path.isFile()) {
                    return new FileSystemResource(path).getInputStream();
                }

                return new FileSystemResource(createZip(path)).getInputStream();
            });
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

    private static Mono<Integer> getMemory(Map<String, String> manifest) {
        return Mono.just(manifest)
            .map(m -> m.get("memory"))
            .map(AbstractApplication::resolveMemory);
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
