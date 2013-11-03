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
public final class ApplicationsRule implements TestRule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private final ServiceRule serviceRule;

    private volatile Application application;

    @Autowired
    ApplicationsRule(CloudFoundryOperations cloudFoundryOperations, ServiceRule serviceRule) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.serviceRule = serviceRule;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                ApplicationsRule.this.application = createApplication(description);
                base.evaluate();
                IoUtils.deleteQuietly(ApplicationsRule.this.application);
            }
        };
    }

    public Application getApplication() {
        return this.application;
    }

    private Application createApplication(Description description) {
        Applications annotation = description.getAnnotation(Applications.class);

        String name;
        if (annotation == null) {
            this.logger.debug("No @Applications annotation found");
            name = null;
        } else {
            name = annotation.value();
            this.logger.debug("Found @Applications annotation with {}", name);
        }

        Application application;
        if (name != null) {
            application = new CloudFoundryApplication(this.cloudFoundryOperations, name);
            application.push().bind(this.serviceRule.getServices()).start();
        } else {
            application = null;
        }

        return application;
    }

}
