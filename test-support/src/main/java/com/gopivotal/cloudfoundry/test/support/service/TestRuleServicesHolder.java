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
import com.gopivotal.cloudfoundry.test.support.util.TcfUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.Arrays;

@Component
final class TestRuleServicesHolder extends AbstractServicesHolder implements TestRule {

    private final CloudFoundryOperations cloudFoundryOperations;

    private final RandomizedNameFactory randomizedNameFactory;

    @Autowired
    TestRuleServicesHolder(CloudFoundryOperations cloudFoundryOperations, RandomizedNameFactory randomizedNameFactory) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.randomizedNameFactory = randomizedNameFactory;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Wrapper(base, this.cloudFoundryOperations, description, this.randomizedNameFactory, this);
    }

    private static final class Wrapper extends Statement {

        private final Logger logger = LoggerFactory.getLogger(this.getClass());

        private final Statement base;

        private final CloudFoundryOperations cloudFoundryOperations;

        private final Description description;

        private final RandomizedNameFactory randomizedNameFactory;

        private final ServicesHolder servicesHolder;

        private Wrapper(Statement base, CloudFoundryOperations cloudFoundryOperations, Description description,
                        RandomizedNameFactory randomizedNameFactory, ServicesHolder servicesHolder) {
            this.base = base;
            this.cloudFoundryOperations = cloudFoundryOperations;
            this.description = description;
            this.randomizedNameFactory = randomizedNameFactory;
            this.servicesHolder = servicesHolder;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                createServices();
                this.base.evaluate();
            } finally {
                TcfUtils.deleteQuietly(this.servicesHolder.get());
                this.servicesHolder.clear();
            }
        }

        @SuppressWarnings("unchecked")
        private void createServices() {
            CreateServices annotation = this.description.getAnnotation(CreateServices.class);

            Class<? extends Service>[] serviceClasses;
            if (annotation == null) {
                this.logger.debug("No @CreateServices annotation found");
                serviceClasses = new Class[0];
            } else {
                serviceClasses = annotation.value();
                this.logger.debug("Found @CreateServices annotation with {}", Arrays.toString(serviceClasses));
            }

            for (Class<? extends Service> serviceClass : serviceClasses) {

                Constructor<? extends Service> constructor = getConstructorIfAvailable(serviceClass);
                this.servicesHolder.add(BeanUtils.instantiateClass(constructor, this.cloudFoundryOperations,
                        this.randomizedNameFactory));
            }
        }

        @SuppressWarnings("unchecked")
        private Constructor<? extends Service> getConstructorIfAvailable(Class<? extends Service> serviceClass) {
            for (Constructor<?> constructor : serviceClass.getDeclaredConstructors()) {
                if (isConstructor(constructor)) {
                    return (Constructor<? extends Service>) constructor;
                }
            }

            throw new IllegalArgumentException(String.format("%s does not have a constructor that takes a " +
                    "CloudFoundryOperations and a RandomizedNameFactory", serviceClass.getSimpleName()));
        }

        private boolean isConstructor(Constructor<?> constructor) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            return parameterTypes.length == 2 && CloudFoundryOperations.class.isAssignableFrom(parameterTypes[0]) &&
                    RandomizedNameFactory.class.isAssignableFrom(parameterTypes[1]);
        }

    }
}
