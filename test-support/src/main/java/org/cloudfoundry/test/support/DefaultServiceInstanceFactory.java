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

import org.cloudfoundry.client.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
final class DefaultServiceInstanceFactory implements ServiceInstanceFactory {

    private final CloudFoundryClient cloudFoundryClient;

    private final Environment environment;

    private final Mono<String> spaceId;

    @Autowired
    DefaultServiceInstanceFactory(CloudFoundryClient cloudFoundryClient, Environment environment, Mono<String> spaceId) {
        this.cloudFoundryClient = cloudFoundryClient;
        this.environment = environment;
        this.spaceId = spaceId;
    }

    @Override
    public ServiceInstance get(String type) {
        return new DefaultServiceInstance(this.cloudFoundryClient, getName(this.environment, type), getService(this.environment, type), getPlan(this.environment, type), this.spaceId);
    }

    private static String getName(Environment environment, String type) {
        return environment.getRequiredProperty(String.format("services.%s.name", type));
    }

    private static String getPlan(Environment environment, String type) {
        return environment.getRequiredProperty(String.format("services.%s.plan", type));
    }

    private static String getService(Environment environment, String type) {
        return environment.getRequiredProperty(String.format("services.%s.service", type));
    }

}
