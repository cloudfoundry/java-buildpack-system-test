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
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;

import static reactor.util.concurrent.Queues.SMALL_BUFFER_SIZE;
import static reactor.util.concurrent.Queues.XS_BUFFER_SIZE;

@Configuration
class ApplicationConfiguration {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<Application> applications;

    @Autowired
    private CloudFoundryOperations cloudFoundryOperations;

    @PreDestroy
    void delete() {
        this.cloudFoundryOperations.applications()
            .list()
            .map(ApplicationSummary::getName)
            .flatMapDelayError(applicationName -> this.cloudFoundryOperations.applications()
                    .delete(DeleteApplicationRequest.builder()
                        .deleteRoutes(true)
                        .name(applicationName)
                        .build())
                    .doOnError(t -> this.logger.error("Error deleting {}", applicationName, t))
                    .doOnSubscribe(s -> this.logger.info("Deleting {}", applicationName)),
                SMALL_BUFFER_SIZE, XS_BUFFER_SIZE)
            .then()
            .block(Duration.ofMinutes(15));
    }

    @PostConstruct
    void push() {
        delete();

        Flux
            .fromIterable(this.applications)
            .flatMap(Application::push)
            .then()
            .block(Duration.ofMinutes(15));
    }
}
