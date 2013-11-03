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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Random;

abstract class AbstractService implements Service {

    private static final Random RANDOM = new SecureRandom();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private final String name;

    protected AbstractService(CloudFoundryOperations cloudFoundryOperations, String label, String plan) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.name = buildName(label);

        this.logger.info("Creating service {}", this.name);

        CloudServiceOffering cloudServiceOffering = getServiceOffering(cloudFoundryOperations, label);
        createCloudService(cloudFoundryOperations, cloudServiceOffering, this.name, label, plan);
    }

    private static CloudServiceOffering getServiceOffering(CloudFoundryOperations cloudFoundryOperations,
                                                           String label) {
        for (CloudServiceOffering cloudServiceOffering : cloudFoundryOperations.getServiceOfferings()) {
            if (label.equals(cloudServiceOffering.getLabel())) {
                return cloudServiceOffering;
            }
        }

        throw new IllegalArgumentException(String.format("No service type with a label of %s", label));
    }

    private static void createCloudService(CloudFoundryOperations cloudFoundryOperations,
                                           CloudServiceOffering cloudServiceOffering, String name, String label,
                                           String plan) {
        CloudService cloudService = new CloudService(CloudEntity.Meta.defaultMeta(), name);
        cloudService.setProvider("core");
        cloudService.setLabel(label);
        cloudService.setVersion(cloudServiceOffering.getVersion());
        cloudService.setPlan(plan);

        cloudFoundryOperations.createService(cloudService);
    }

    private static String buildName(String label) {
        return String.format("system-test-%s-%06d", label, RANDOM.nextInt(1000000));
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final void delete() {
        this.logger.debug("Deleting service {}", this.name);
        this.cloudFoundryOperations.deleteService(this.name);
    }

}
