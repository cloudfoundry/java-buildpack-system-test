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

import com.gopivotal.cloudfoundry.test.support.util.TcfUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

final class YamlManifest implements Manifest {

    private final File applicationPath;

    private final Map<String, Object> contents;

    private final String defaultBuildpack;

    private final MemorySizeParser memorySizeParser;

    YamlManifest(File applicationPath, String defaultBuildpack, MemorySizeParser memorySizeParser) {
        this.applicationPath = applicationPath;
        this.defaultBuildpack = defaultBuildpack;
        this.contents = readContents(applicationPath);
        this.memorySizeParser = memorySizeParser;
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
            TcfUtils.closeQuietly(in);
        }
    }

    @Override
    public String getBuildpack() {
        String raw = (String) this.contents.get("buildpack");
        return raw == null ? this.defaultBuildpack : raw;
    }

    @Override
    public Integer getInstances() {
        return (Integer) this.contents.get("instances");
    }

    @Override
    public Integer getMemory() {
        String raw = (String) this.contents.get("memory");
        return this.memorySizeParser.toMibiBytes(raw);
    }

    @Override
    public String getName() {
        return (String) this.contents.get("name");
    }

    @Override
    public File getPath() {
        String relativePath = (String) this.contents.get("path");
        return new File(this.applicationPath, relativePath);
    }
}
