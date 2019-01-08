/*
 * Copyright 2013-2019 the original author or authors.
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

package org.cloudfoundry.test.support.service;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "services.mongodb.enabled", matchIfMissing = true)
public final class MongoDbServiceInstance extends AbstractServiceInstance {

    @Autowired
    MongoDbServiceInstance(CloudFoundryOperations cloudFoundryOperations,
                           @Value("${services.mongodb.name}") String name,
                           @Value("${services.mongodb.plan}") String plan,
                           @Value("${services.mongodb.service}") String service) {

        super(cloudFoundryOperations, name, plan, service);
    }

    @Override
    String extractEndpoint(Map<String, Object> credentials) {
        URI uri = URI.create((String) credentials.get("uri"));

        return String.format("%s://%s:%s%s", uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath());
    }

}
