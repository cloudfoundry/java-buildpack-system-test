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

package com.gopivotal.cloudfoundry.test.support.application;

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Order(0)
final class ApplicationCleanupTestRule implements TestRule {

    private final CloudFoundryOperations cloudFoundryOperations;

    private final String domain;

    private final AtomicBoolean finished = new AtomicBoolean();

    private final Object monitor = new Object();

    private final RandomizedNameFactory randomizedNameFactory;

    @Autowired
    ApplicationCleanupTestRule(CloudFoundryOperations cloudFoundryOperations, String domain,
                               RandomizedNameFactory randomizedNameFactory) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.domain = domain;
        this.randomizedNameFactory = randomizedNameFactory;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Wrapper(base, this.cloudFoundryOperations, this.domain, this.finished, this.monitor,
                this.randomizedNameFactory);
    }

    private static final class Wrapper extends Statement {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Statement base;

        private final CloudFoundryOperations cloudFoundryOperations;

        private final String domain;

        private final AtomicBoolean finished;

        private final Object monitor;

        private final RandomizedNameFactory randomizedNameFactory;

        private Wrapper(Statement base, CloudFoundryOperations cloudFoundryOperations, String domain, AtomicBoolean finished,
                        Object monitor, RandomizedNameFactory randomizedNameFactory) {
            this.base = base;
            this.cloudFoundryOperations = cloudFoundryOperations;
            this.domain = domain;
            this.finished = finished;
            this.monitor = monitor;
            this.randomizedNameFactory = randomizedNameFactory;
        }

        @Override
        public void evaluate() throws Throwable {
            synchronized (this.monitor) {
                if (!this.finished.get()) {
                    this.logger.info("Cleaning up residual applications");

                    for (CloudApplication cloudApplication : this.cloudFoundryOperations.getApplications()) {
                        String name = cloudApplication.getName();

                        if (this.randomizedNameFactory.matches(name)) {
                            this.logger.warn("Deleting residual application {}", name);
                            this.cloudFoundryOperations.deleteRoute(name, this.domain);
                            this.cloudFoundryOperations.deleteApplication(name);
                        }
                    }

                    this.finished.set(true);
                }
            }

            this.base.evaluate();
        }
    }
}
