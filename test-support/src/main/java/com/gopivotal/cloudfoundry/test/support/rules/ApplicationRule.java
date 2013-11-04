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

package com.gopivotal.cloudfoundry.test.support.rules;

import com.gopivotal.cloudfoundry.test.support.application.Application;
import com.gopivotal.cloudfoundry.test.support.application.CloudFoundryApplication;
import com.gopivotal.cloudfoundry.test.support.util.IoUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
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
public final class ApplicationRule implements TestRule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private final ServicesRule servicesRule;

    private volatile Application application;

    @Autowired
    ApplicationRule(CloudFoundryOperations cloudFoundryOperations, ServicesRule servicesRule) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.servicesRule = servicesRule;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                ApplicationRule.this.application = createApplication(description);
                base.evaluate();
                IoUtils.deleteQuietly(ApplicationRule.this.application);
            }
        };
    }

    public Application getApplication() {
        return this.application;
    }

    private Application createApplication(Description description) {
        CreateApplication annotation = description.getAnnotation(CreateApplication.class);

        String name;
        if (annotation == null) {
            this.logger.debug("No @CreateApplication annotation found");
            name = null;
        } else {
            name = annotation.value();
            this.logger.debug("Found @CreateApplication annotation with {}", name);
        }

        Application application;
        if (name != null) {
            application = new CloudFoundryApplication(this.cloudFoundryOperations, name);
            application.push().bind(this.servicesRule.getServices()).start();
        } else {
            application = null;
        }

        return application;
    }

}
