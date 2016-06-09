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
import org.cloudfoundry.test.support.service.ServiceInstance;
import org.cloudfoundry.util.DelayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

final class ServiceBindingTestExecutionListener extends AbstractTestExecutionListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterTestClass(TestContext testContext) {
        getServiceType(testContext).ifPresent(serviceType -> {
            Collection<Application> applications = getApplications(testContext);
            ServiceInstance serviceInstance = getServiceInstance(testContext, serviceType);

            Flux
                .fromIterable(applications)
                .flatMap((application) -> serviceInstance.unbind(application)
                    .doOnError(t -> this.logger.warn("Error while unbinding: {}", t.getMessage()))
                    .retryWhen(DelayUtils.exponentialBackOffError(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofMinutes(1))))
                .then()
                .block(Duration.ofMinutes(1));
        });
    }

    @Override
    public void beforeTestClass(TestContext testContext) {
        getServiceType(testContext).ifPresent(serviceType -> {
            Collection<Application> applications = getApplications(testContext);
            ServiceInstance serviceInstance = getServiceInstance(testContext, serviceType);

            Flux
                .fromIterable(applications)
                .flatMap(application -> serviceInstance.bind(application)
                    .doOnError(t -> this.logger.warn("Error while binding: {}", t.getMessage()))
                    .retryWhen(DelayUtils.exponentialBackOffError(Duration.ofSeconds(1), Duration.ofSeconds(10), Duration.ofMinutes(1)))
                    .then(application::restage))
                .then()
                .block(Duration.ofMinutes(15));
        });
    }

    private static Collection<Application> getApplications(TestContext testContext) {
        return testContext.getApplicationContext().getBeansOfType(Application.class).values();
    }

    private static ServiceInstance getServiceInstance(TestContext testContext, ServiceType annotation) {
        return testContext.getApplicationContext().getBean(annotation.value());
    }

    private static Optional<ServiceType> getServiceType(TestContext testContext) {
        return Optional.ofNullable(AnnotationUtils.findAnnotation(testContext.getTestClass(), ServiceType.class));
    }

}
