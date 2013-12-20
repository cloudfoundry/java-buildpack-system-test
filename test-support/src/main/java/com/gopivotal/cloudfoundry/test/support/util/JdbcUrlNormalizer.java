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

import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public final class JdbcUrlNormalizer {

    private JdbcUrlNormalizer() {
    }

    /**
     * Normalize a JDBC URL to account for differences in underlying implementations when testing.  The normalization
     * algorithm includes removing the {@literal jdbc} prefix and any query parameters, leaving the core part of the URL
     * (i.e. scheme, host, port, and path).
     *
     * @param raw the raw URL
     *
     * @return a normalized URL
     */
    public static URI normalize(String raw) {
        String modified = raw;

        if (modified.startsWith("jdbc:")) {
            modified = modified.replaceFirst("jdbc:", "");
        }

        if (modified.startsWith("postgres:")) {
            modified = modified.replaceFirst("postgres:", "postgresql:");
        }

        if (modified.contains("@")) {
            modified = modified.replaceFirst("/[^/@]*@", "/");
        }

        if (modified.contains("?")) {
            modified = modified.substring(0, modified.indexOf('?'));
        }

        return URI.create(modified);
    }

}

