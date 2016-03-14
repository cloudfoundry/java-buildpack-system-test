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

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public final class IgnoreOnPropertyTestRule implements TestRule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Environment environment;

    @Autowired
    IgnoreOnPropertyTestRule(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        IgnoreOnProperty annotation = description.getAnnotation(IgnoreOnProperty.class);

        if (annotation == null || this.environment.getProperty(annotation.value(), boolean.class, true)) {
            return base;
        }

        this.logger.debug("Ignoring {}.{}", description.getTestClass().getSimpleName(), description.getMethodName());

        return new IgnoreStatement(annotation.value());
    }

    private static final class IgnoreStatement extends Statement {

        private final String property;

        private IgnoreStatement(String property) {
            this.property = property;
        }

        @Override
        public void evaluate() throws Throwable {
            throw new AssumptionViolatedException(String.format("Property %s set to false", this.property));
        }

    }

}
