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

package org.cloudfoundry.test;

import org.cloudfoundry.test.support.application.Application;
import org.cloudfoundry.test.support.service.MySqlServiceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.cloudfoundry.util.tuple.TupleUtils.consumer;

@ServiceType(MySqlServiceInstance.class)
@TestType("mysql")
public final class MySqlAutoReconfigurationTest extends AbstractTest {

    @Autowired
    private MySqlServiceInstance service;

    @Override
    protected void test(Application application) {
        Mono
            .when(
                application.request("/datasource/check-access"),
                this.service.getEndpoint(application), application.request("/datasource/url"))
            .as(StepVerifier::create)
            .assertNext(consumer((access, expectedUrl, actualUrl) -> {
                assertThat(access).isEqualTo("ok");
                assertThat(actualUrl).isEqualTo(expectedUrl);
            }))
            .expectComplete()
            .verify(Duration.ofMinutes(5));
    }

}
