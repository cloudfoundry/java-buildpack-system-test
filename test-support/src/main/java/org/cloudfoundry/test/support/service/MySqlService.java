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

package org.cloudfoundry.test.support.service;

import org.cloudfoundry.client.CloudFoundryClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class MySqlService extends AbstractService {

    @Autowired
    MySqlService(CloudFoundryClient cloudFoundryClient,
                 @Value("${services.mysql.name}") String name,
                 @Value("${services.mysql.plan}") String plan,
                 @Value("${services.mysql.service}") String service,
                 Mono<String> spaceId) {
        super(cloudFoundryClient, name, plan, service, spaceId);
    }

}
