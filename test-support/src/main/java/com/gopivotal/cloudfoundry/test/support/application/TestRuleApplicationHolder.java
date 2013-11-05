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

import com.gopivotal.cloudfoundry.test.support.service.ServicesHolder;
import com.gopivotal.cloudfoundry.test.support.util.TcfUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
final class TestRuleApplicationHolder extends AbstractApplicationHolder implements TestRule {

    private final ApplicationFactory applicationFactory;

    private final ServicesHolder servicesHolder;

    @Autowired
    TestRuleApplicationHolder(ApplicationFactory applicationFactory, ServicesHolder servicesHolder) {
        this.applicationFactory = applicationFactory;
        this.servicesHolder = servicesHolder;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Wrapper(this.applicationFactory, this, base, description, this.servicesHolder);
    }

    private static final class Wrapper extends Statement {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final ApplicationFactory applicationFactory;

        private final ApplicationHolder applicationHolder;

        private final Statement base;

        private final Description description;

        private final ServicesHolder servicesHolder;

        private Wrapper(ApplicationFactory applicationFactory, ApplicationHolder applicationHolder,
                        Statement base, Description description, ServicesHolder servicesHolder) {
            this.applicationFactory = applicationFactory;
            this.applicationHolder = applicationHolder;
            this.base = base;
            this.description = description;
            this.servicesHolder = servicesHolder;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                createApplication();
                this.base.evaluate();
            } finally {
                TcfUtils.deleteQuietly(this.applicationHolder.get());
                this.applicationHolder.clear();
            }
        }

        private void createApplication() {
            CreateApplication annotation = this.description.getAnnotation(CreateApplication.class);

            if (annotation == null) {
                this.logger.debug("No @CreateApplication annotation found");
            } else {
                String name = annotation.value();
                this.logger.debug("Found @CreateApplication annotation with {}", name);

                Application application = this.applicationFactory.create(name);
                application.push().bind(this.servicesHolder.get()).start();

                this.applicationHolder.set(application);
            }

        }
    }
}
