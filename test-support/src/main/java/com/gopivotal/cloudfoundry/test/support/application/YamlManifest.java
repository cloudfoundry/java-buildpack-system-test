/*
 * Copyright 2013 the original author or authors.
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

package com.gopivotal.cloudfoundry.test.support.application;

import com.gopivotal.cloudfoundry.test.support.util.IoUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

final class YamlManifest implements Manifest {

    private final String buildpack;

    private final Integer instances;

    private final Integer memory;

    private final String name;

    private final File path;

    /**
     * Creates a new instance
     */
    public YamlManifest(File applicationPath) {
        this(applicationPath, new StandardMemorySizeParser());
    }

    YamlManifest(File applicationPath, MemorySizeParser memorySizeParser) {
        Map<String, Object> contents = readContents(applicationPath);

        this.buildpack = getBuildpack(contents);
        this.instances = getInstances(contents);
        this.memory = getMemory(contents, memorySizeParser);
        this.name = getName(contents);
        this.path = getPath(contents, applicationPath);
    }

    private static String getBuildpack(Map<String, Object> contents) {
        return (String) contents.get("buildpack");
    }

    private static Integer getInstances(Map<String, Object> contents) {
        return (Integer) contents.get("instances");
    }

    private static Integer getMemory(Map<String, Object> contents, MemorySizeParser memorySizeParser) {
        return memorySizeParser.toMibiBytes((String) contents.get("memory"));
    }

    private static File getPath(Map<String, Object> contents, File applicationPath) {
        return new File(applicationPath, (String) contents.get("path"));
    }

    private static String getName(Map<String, Object> contents) {
        return (String) contents.get("name");
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> readContents(File applicationPath) {
        InputStream in = null;
        try {
            in = new FileInputStream(new File(applicationPath, "manifest.yml"));
            return ((List<Map<String, Object>>) new Yaml().loadAs(in, Map.class).get("applications")).get(0);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            IoUtils.closeQuietly(in);
        }
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the memory size
     *
     * @return the memory size
     */
    @Override
    public int getMemory() {
        return this.memory;
    }

    /**
     * Returns the number of instances
     *
     * @return the number of instances
     */
    @Override
    public int getInstances() {
        return this.instances;
    }

    /**
     * Returns the path to the application
     *
     * @return the path to the application
     */
    @Override
    public File getPath() {
        return this.path;
    }

    /**
     * Returns the URI of the buildpack
     *
     * @return the URI of the buildpack
     */
    @Override
    public String getBuildpack() {
        return this.buildpack;
    }
}
