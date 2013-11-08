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

import com.gopivotal.cloudfoundry.test.support.operations.TestOperations;
import com.gopivotal.cloudfoundry.test.support.operations.TestOperationsFactory;
import com.gopivotal.cloudfoundry.test.support.service.Service;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

final class CloudFoundryApplication implements Application {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private final Manifest manifest;

    private final String name;

    private final TestOperations testOperations;

    CloudFoundryApplication(CloudFoundryOperations cloudFoundryOperations, Manifest manifest, String name,
                            TestOperationsFactory testOperationsFactory) {
        this.logger.info("Creating application {}", name);

        this.cloudFoundryOperations = cloudFoundryOperations;
        String host = buildHost(name, getDomain(cloudFoundryOperations));
        this.manifest = manifest;
        this.name = name;
        this.testOperations = testOperationsFactory.create(host);

        createCloudApplication(cloudFoundryOperations, host, manifest, name);
    }

    private static String buildHost(String name, String domain) {
        return String.format("%s.%s", name, domain);
    }

    private static void createCloudApplication(CloudFoundryOperations cloudFoundryOperations, String host,
                                               Manifest manifest, String name) {
        Staging staging = new Staging(null, manifest.getBuildpack());
        cloudFoundryOperations.createApplication(name, staging, manifest.getMemory(), Arrays.asList(host),
                Collections.<String>emptyList());
    }

    private static String getDomain(CloudFoundryOperations cloudFoundryOperations) {
        for (CloudDomain domain : cloudFoundryOperations.getDomainsForOrg()) {
            if (domain.getOwner().getName().equals("none")) {
                return domain.getName();
            }
        }

        throw new IllegalStateException("No default domain found");
    }

    @Override
    public Application bind(Service... services) {
        for (Service service : services) {
            String serviceName = service.getName();

            this.logger.info("Binding service {}", serviceName);
            this.cloudFoundryOperations.bindService(this.name, serviceName);
        }

        return this;
    }

    @Override
    public void delete() {
        if (applicationExists()) {
            this.logger.debug("Deleting application {}", this.name);
            this.cloudFoundryOperations.deleteApplication(this.name);
        }
    }

    @Override
    public TestOperations getTestOperations() {
        return this.testOperations;
    }

    @Override
    public Application push() {
        try {
            this.logger.debug("Pushing application {}", this.name);
            this.cloudFoundryOperations.uploadApplication(this.name, this.manifest.getPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    @Override
    public Application start() {
        this.logger.info("Starting application {}", this.name);
        this.cloudFoundryOperations.startApplication(this.name);
        return this;
    }

    @Override
    public Application stop() {
        this.logger.debug("Stopping application {}", this.name);
        this.cloudFoundryOperations.stopApplication(this.name);
        return this;
    }

    @Override
    public Application unbind(Service... services) {
        for (Service service : services) {
            String serviceName = service.getName();

            this.logger.debug("Unbinding service {}", serviceName);
            this.cloudFoundryOperations.unbindService(this.name, serviceName);
        }

        return this;
    }

    public String getName() {
        return this.name;
    }

    private Boolean applicationExists() {
        for (CloudApplication cloudApplication : this.cloudFoundryOperations.getApplications()) {
            if (cloudApplication.getName().equals(this.name)) {
                return true;
            }
        }

        return false;
    }

}
