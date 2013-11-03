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

import com.gopivotal.cloudfoundry.test.support.service.Service;
import com.gopivotal.cloudfoundry.test.support.util.IoUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Order(-1)
final class ServiceRule implements TestRule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private volatile Service[] services;

    @Autowired
    ServiceRule(CloudFoundryOperations cloudFoundryOperations) {
        this.cloudFoundryOperations = cloudFoundryOperations;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                ServiceRule.this.services = createServices(description);
                base.evaluate();
                IoUtils.deleteQuietly(ServiceRule.this.services);
            }

        };
    }

    public Service[] getServices() {
        return this.services;
    }

    @SuppressWarnings("unchecked")
    private Service[] createServices(Description description) {
        Services annotation = description.getAnnotation(Services.class);

        Class<? extends Service>[] serviceClasses;
        if (annotation == null) {
            this.logger.debug("No @Services annotation found");
            serviceClasses = new Class[0];
        } else {
            serviceClasses = annotation.value();
            this.logger.debug("Found @Services annotation with {}", Arrays.toString(serviceClasses));
        }

        List<Service> services = new ArrayList<>(serviceClasses.length);
        for (Class<? extends Service> serviceClass : serviceClasses) {
            Constructor<? extends Service> constructor = ClassUtils.getConstructorIfAvailable(serviceClass,
                    CloudFoundryOperations.class);
            services.add(BeanUtils.instantiateClass(constructor, this.cloudFoundryOperations));
        }

        return services.toArray(new Service[services.size()]);
    }
}
