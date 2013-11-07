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

import com.gopivotal.cloudfoundry.test.support.operations.TestOperationsFactory;
import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
final class CloudFoundryApplicationFactory implements ApplicationFactory {

    private final CloudFoundryOperations cloudFoundryOperations;

    private final ManifestFactory manifestFactory;

    private final RandomizedNameFactory randomizedNameFactory;

    private final TestOperationsFactory testOperationsFactory;

    @Autowired
    CloudFoundryApplicationFactory(CloudFoundryOperations cloudFoundryOperations, ManifestFactory manifestFactory,
                                   RandomizedNameFactory randomizedNameFactory,
                                   TestOperationsFactory testOperationsFactory) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.manifestFactory = manifestFactory;
        this.randomizedNameFactory = randomizedNameFactory;
        this.testOperationsFactory = testOperationsFactory;
    }

    @Override
    public Application create(String name) {
        Manifest manifest = this.manifestFactory.create(applicationPath(name));
        String randomizedName = this.randomizedNameFactory.create(name);

        return new CloudFoundryApplication(this.cloudFoundryOperations, manifest, randomizedName,
                this.testOperationsFactory);
    }

    private File applicationPath(String name) {
        return new File(new File("../vendor/java-test-applications"), String.format("%s-application", name));
    }

}
