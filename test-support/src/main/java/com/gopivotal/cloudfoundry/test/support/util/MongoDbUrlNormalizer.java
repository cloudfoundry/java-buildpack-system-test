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

import java.net.URI;

public final class MongoDbUrlNormalizer {

    private MongoDbUrlNormalizer() {
    }

    /**
     * Normalize a MongoDb URL to account for differences in underlying implementations when testing. The normalization
     * removes credentials, leaving the core part of the URL (i.e. scheme, host, port, and path).
     *
     * @param raw the raw URL
     *
     * @return a normalized URL
     */
    public static URI normalize(String raw) {
        String modified = raw;

        if (modified.contains("@")) {
            modified = modified.replaceFirst("/[^/@]*@", "/");
        }

        return URI.create(modified);
    }

}

