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

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.applications.ApplicationEnvironments;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.cloudfoundry.test.support.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

abstract class AbstractServiceInstance implements ServiceInstance {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private final String name;

    private final String plan;

    private final String service;

    AbstractServiceInstance(CloudFoundryOperations cloudFoundryOperations, String name, String plan, String service) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.name = name;
        this.plan = plan;
        this.service = service;
    }

    @Override
    public final Mono<Void> bind(Application application) {
        return this.cloudFoundryOperations.services()
            .bind(BindServiceInstanceRequest.builder()
                .applicationName(application.getName())
                .serviceInstanceName(this.name)
                .build())
            .doOnSubscribe(s -> this.logger.info("Binding {} to {}", this.name, application.getName()));
    }

    @Override
    public Mono<Void> create() {
        return this.cloudFoundryOperations.services()
            .listInstances()
            .filter(serviceInstance -> this.name.equals(serviceInstance.getName()))
            .singleOrEmpty()
            .otherwiseIfEmpty(this.cloudFoundryOperations.services()
                .createInstance(CreateServiceInstanceRequest.builder()
                    .plan(this.plan)
                    .service(this.service)
                    .serviceInstance(this.name)
                    .build())
                .cast(org.cloudfoundry.operations.services.ServiceInstance.class)
                .doOnSubscribe(s -> this.logger.info("Creating {} ({}/{})", this.name, this.service, this.plan)))
            .after();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Mono<String> getEndpoint(Application application) {
        return this.cloudFoundryOperations.applications()
            .getEnvironments(GetApplicationEnvironmentsRequest.builder()
                .name(application.getName())
                .build())
            .map(ApplicationEnvironments::getSystemProvided)
            .map(environment -> (Map<String, List<Map<String, Object>>>) environment.get("VCAP_SERVICES"))
            .flatMap(services -> Flux.fromIterable(services.values()))
            .flatMap(Flux::fromIterable)
            .filter(si -> this.name.equals(si.get("name")))
            .single()
            .map(si -> (Map<String, Object>) si.get("credentials"))
            .map(this::extractEndpoint);
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final Mono<Void> unbind(Application application) {
        return this.cloudFoundryOperations.services()
            .unbind(UnbindServiceInstanceRequest.builder()
                .applicationName(application.getName())
                .serviceInstanceName(this.name)
                .build())
            .doOnSubscribe(s -> this.logger.info("Unbinding {} from {}", this.name, application.getName()));
    }

    abstract String extractEndpoint(Map<String, Object> credentials);

}
