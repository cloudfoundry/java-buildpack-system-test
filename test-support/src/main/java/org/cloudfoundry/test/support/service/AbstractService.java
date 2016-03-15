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
import reactor.core.publisher.Mono;

abstract class AbstractService implements Service {

    private final String name;

    protected AbstractService(CloudFoundryClient cloudFoundryClient, String name, String plan, String service, Mono<String> spaceId) {
        this.name = name;
    }

    @Override
    public Mono<String> getName() {
        return Mono.just(this.name);
    }

}
