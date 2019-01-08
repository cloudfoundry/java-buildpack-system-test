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

package org.cloudfoundry.test.support.application;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.test.support.NameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestOperations;

import java.io.File;

@Component
@ConditionalOnProperty(name = "applications.springBootCliJar.enabled", matchIfMissing = true)
public final class SpringBootCliJarApplication extends AbstractApplication {

    @Autowired
    SpringBootCliJarApplication(String buildpack,
                                CloudFoundryOperations cloudFoundryOperations,
                                @Value("${applications.springBootCliJar.location}") File location,
                                @Value("${applications.springBootCliJar.memory:#{null}}") String memory,
                                NameFactory nameFactory,
                                @Value("${applications.springBootCliJar.prefix}") String prefix,
                                AsyncRestOperations restOperations) {
        super(buildpack, cloudFoundryOperations, location, memory, nameFactory.getName(prefix), restOperations);
    }

}
