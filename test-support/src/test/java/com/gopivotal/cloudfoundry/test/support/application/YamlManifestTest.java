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

import org.junit.Test;

import java.io.File;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public final class YamlManifestTest {

    private final YamlManifest manifest;

    public YamlManifestTest() {
        MemorySizeParser memorySizeParser = createMemorySizeParser();
        this.manifest = createManifest(memorySizeParser);
    }

    private static MemorySizeParser createMemorySizeParser() {
        MemorySizeParser memorySizeParser = mock(MemorySizeParser.class);
        when(memorySizeParser.toMibiBytes("512M")).thenReturn(512);

        return memorySizeParser;
    }

    private static YamlManifest createManifest(MemorySizeParser memorySizeParser) {
        return new YamlManifest(new File("src/test/resources/manifest-with-buildpack"), "test-default-buildpack",
                memorySizeParser);
    }

    @Test
    public void getBuildpackSpecified() {
        assertEquals("test-buildpack", this.manifest.getBuildpack());
    }

    @Test
    public void getBuildpackNotSpecified() {
        Manifest manifest = new YamlManifest(new File("src/test/resources/manifest-without-buildpack"),
                "test-default-buildpack", null);
        assertEquals("test-default-buildpack", manifest.getBuildpack());
    }

    @Test
    public void getInstances() {
        assertEquals(Integer.valueOf(8), this.manifest.getInstances());
    }

    @Test
    public void getMemory() {
        assertEquals(Integer.valueOf(512), this.manifest.getMemory());
    }

    @Test
    public void getName() {
        assertEquals("test-name", this.manifest.getName());
    }

    @Test
    public void getPath() {
        assertEquals(new File("src/test/resources/manifest-with-buildpack/test-path"), this.manifest.getPath());
    }

}
