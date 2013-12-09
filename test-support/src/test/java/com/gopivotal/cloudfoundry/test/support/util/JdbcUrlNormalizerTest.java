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

package com.gopivotal.cloudfoundry.test.support.util;

import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public final class JdbcUrlNormalizerTest {

    @Test
    public void removeJdbc() {
        URI result = JdbcUrlNormalizer.normalize("jdbc:relational://test.host");
        assertEquals(URI.create("relational://test.host"), result);
    }

    @Test
    public void adjustPostgres() {
        URI result = JdbcUrlNormalizer.normalize("postgres://test.host");
        assertEquals(URI.create("postgresql://test.host"), result);
    }

    @Test
    public void removeQuery() {
        URI result = JdbcUrlNormalizer.normalize("relational://test.host?query=parameter");
        assertEquals(URI.create("relational://test.host"), result);
    }

    @Test
    public void removeUserInfo() {
        URI result = JdbcUrlNormalizer.normalize("relational://user:info@test.host");
        assertEquals(URI.create("relational://test.host"), result);
    }

    @Test
    public void withPort() {
        URI result = JdbcUrlNormalizer.normalize("relational://test.host:1234");
        assertEquals(URI.create("relational://test.host:1234"), result);
    }

    @Test
    public void withPath() {
        URI result = JdbcUrlNormalizer.normalize("relational://test.host/path");
        assertEquals(URI.create("relational://test.host/path"), result);
    }

    @Test
    public void h2() {
        URI result = JdbcUrlNormalizer.normalize("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        assertEquals(URI.create("h2:mem:testdb;DB_CLOSE_DELAY=-1"), result);
    }

}
