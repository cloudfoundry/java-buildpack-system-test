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

import static org.junit.Assert.assertEquals;

public final class StandardMemorySizeParserTest {

    private final StandardMemorySizeParser memorySizeParser = new StandardMemorySizeParser();

    @Test(expected = IllegalArgumentException.class)
    public void tooShort() {
        this.memorySizeParser.toMibiBytes("1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notNumber() {
        this.memorySizeParser.toMibiBytes("aG");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notPositive() {
        this.memorySizeParser.toMibiBytes("-1G");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unknownUnit() {
        this.memorySizeParser.toMibiBytes("1T");
    }

    @Test
    public void gibiUppercase() {
        assertEquals(Integer.valueOf(1024), this.memorySizeParser.toMibiBytes("1G"));
    }

    @Test
    public void gibiLowercase() {
        assertEquals(Integer.valueOf(1024), this.memorySizeParser.toMibiBytes("1g"));
    }

    @Test
    public void mibiUppercase() {
        assertEquals(Integer.valueOf(1), this.memorySizeParser.toMibiBytes("1M"));
    }

    @Test
    public void mibiLowercase() {
        assertEquals(Integer.valueOf(1), this.memorySizeParser.toMibiBytes("1m"));
    }
}
