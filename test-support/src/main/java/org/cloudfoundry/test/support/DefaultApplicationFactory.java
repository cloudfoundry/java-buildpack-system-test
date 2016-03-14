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

package org.cloudfoundry.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;

@Component
final class DefaultApplicationFactory implements ApplicationFactory {

    private final String buildpack;

    private final CloudFoundryOperations cloudFoundryOperations;

    private final Environment environment;

    private final NameFactory nameFactory;

    private final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    @Autowired
    DefaultApplicationFactory(String buildpack, CloudFoundryOperations cloudFoundryOperations, Environment environment, NameFactory nameFactory) {
        this.buildpack = buildpack;
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.environment = environment;
        this.nameFactory = nameFactory;
    }

    @Override
    public Application get(String name) throws IOException {
        File location = getLocation(this.environment, name);
        Map<String, String> manifest = getManifest(location, objectMapper);

        return new DefaultApplication(this.cloudFoundryOperations, getRandomName(this.environment, name, this.nameFactory), this.buildpack, getMemory(manifest), getApplication(location, manifest));
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

    private static Resource getApplication(File location, Map<String, String> manifest) throws IOException {
        File path = new File(location, manifest.get("path"));

        if (path.isFile()) {
            return new FileSystemResource(path);
        }

        return new FileSystemResource(createZip(path));
    }

    private static File getLocation(Environment environment, String name) {
        return environment.getRequiredProperty(String.format("applications.%s.location", name), File.class);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> getManifest(File location, ObjectMapper objectMapper) throws IOException {
        Map manifest = objectMapper.readValue(new File(location, "manifest.yml"), Map.class);
        return ((List<Map<String, String>>) manifest.get("applications")).get(0);
    }

    private static Integer getMemory(Map<String, String> manifest) {
        String s = manifest.get("memory");

        Character last = s.charAt(s.length() - 1);
        if (Character.isDigit(last)) {
            return Integer.parseInt(s);
        }

        if (Character.toLowerCase(last) == 'g') {
            return Integer.parseInt(s.substring(0, s.length() - 1)) * 1024;
        }

        if (Character.toLowerCase(last) == 'm') {
            return Integer.parseInt(s.substring(0, s.length() - 1));
        }

        throw new IllegalArgumentException(String.format("Illegal memory size %s", s));
    }

    private static String getRandomName(Environment environment, String name, NameFactory nameFactory) {
        String prefix = environment.getRequiredProperty(String.format("applications.%s.prefix", name), String.class);
        return nameFactory.getName(prefix);
    }

}
