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

package com.gopivotal.cloudfoundry.test.support.service;

import com.gopivotal.cloudfoundry.test.support.util.MongoDbUrlNormalizer;
import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;

import java.net.URI;
import java.util.Map;

/**
 * Represents an instance of the Redis service
 */
public final class MongoDbService extends AbstractService {

    MongoDbService(CloudFoundryOperations cloudFoundryOperations, RandomizedNameFactory randomizedNameFactory) {
        super(cloudFoundryOperations, "mongolab", "sandbox", randomizedNameFactory);
    }

    @Override
    public final URI getEndpoint(Map<String, String> environmentVariables) {
        Map<String, ?> credentials = getCredentials(environmentVariables);
        String uriString = credentials.get("uri").toString();
        return MongoDbUrlNormalizer.normalize(uriString);
    }
}
