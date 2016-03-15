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

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;

final class DefaultApplication implements Application {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Resource application;

    private final String buildpack;

    private final CloudFoundryOperations cloudFoundryOperations;

    private final Integer memory;

    private final String name;

    DefaultApplication(CloudFoundryOperations cloudFoundryOperations, String name, String buildpack, Integer memory, Resource application) {
        this.application = application;
        this.buildpack = buildpack;
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.memory = memory;
        this.name = name;
    }

    @Override
    public void close() {
        delete().get(Duration.ofMinutes(1));
    }

    @Override
    public Mono<Void> delete() {
        return this.cloudFoundryOperations.applications()
            .delete(DeleteApplicationRequest.builder()
                .deleteRoutes(true)
                .name(this.name)
                .build())
            .doOnSubscribe(s -> this.logger.info("Deleting {}", this.name))
            .doOnError(Throwable::printStackTrace);
    }

    @Override
    public Mono<String> host() {
        return this.cloudFoundryOperations.applications()
            .get(GetApplicationRequest.builder()
                .name(this.name)
                .build())
            .map(applicationDetail -> applicationDetail.getUrls().get(0));
    }

    @Override
    public Mono<Void> push() throws IOException {
        return this.cloudFoundryOperations.applications()
            .push(PushApplicationRequest.builder()
                .application(this.application.getInputStream())
                .buildpack(this.buildpack)
                .memory(this.memory)
                .name(this.name)
                .noStart(true)
                .build())
            .doOnSubscribe(s -> this.logger.info("Pushing {}", this.name))
            .doOnError(Throwable::printStackTrace);
    }

    @Override
    public Mono<Void> start() {
        return this.cloudFoundryOperations.applications()
            .start(StartApplicationRequest.builder()
                .name(this.name)
                .build())
            .doOnSubscribe(s -> this.logger.info("Starting {}", this.name))
            .doOnError(Throwable::printStackTrace);
    }

}
