/*
 * Copyright 2013 the original author or authors.
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

package com.gopivotal.cloudfoundry.test.support.service;

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Order(100)
final class ServiceCleanupTestRule implements TestRule {

    private final Object monitor = new Object();

    private final AtomicBoolean finished = new AtomicBoolean();

    private final CloudFoundryOperations cloudFoundryOperations;

    private final RandomizedNameFactory randomizedNameFactory;

    @Autowired
    ServiceCleanupTestRule(CloudFoundryOperations cloudFoundryOperations, RandomizedNameFactory randomizedNameFactory) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.randomizedNameFactory = randomizedNameFactory;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Wrapper(base, this.cloudFoundryOperations, this.finished, this.monitor, this.randomizedNameFactory);
    }

    private static final class Wrapper extends Statement {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Statement base;

        private final CloudFoundryOperations cloudFoundryOperations;

        private final AtomicBoolean finished;

        private final Object monitor;

        private final RandomizedNameFactory randomizedNameFactory;

        private Wrapper(Statement base, CloudFoundryOperations cloudFoundryOperations, AtomicBoolean finished,
                        Object monitor, RandomizedNameFactory randomizedNameFactory) {
            this.base = base;
            this.cloudFoundryOperations = cloudFoundryOperations;
            this.finished = finished;
            this.monitor = monitor;
            this.randomizedNameFactory = randomizedNameFactory;
        }

        @Override
        public void evaluate() throws Throwable {
            synchronized (this.monitor) {
                if (!this.finished.get()) {
                    this.logger.info("Cleaning up residual services");

                    for (CloudService cloudService : this.cloudFoundryOperations.getServices()) {
                        String name = cloudService.getName();

                        if (this.randomizedNameFactory.matches(name)) {
                            this.logger.warn("Deleting residual service {}", name);

                            try {
                                this.cloudFoundryOperations.deleteService(name);
                            } catch (Exception e) { //HttpClientErrorException | HttpServerErrorException e) {
                                this.logger.error("Unable to delete residual service {}. Caused by '{}'", name,
                                        e.toString());
                            }
                        }
                    }

                    this.finished.set(true);
                }
            }

            this.base.evaluate();
        }
    }
}
