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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;

public abstract class AbstractServiceTest<T extends AbstractService> {

    private final String serviceLabel;
    private final String servicePlan;

    private final CloudService cloudService;

    public AbstractServiceTest(String serviceLabel, String servicePlan) {
        this.serviceLabel = serviceLabel;
        this.servicePlan = servicePlan;
        
        CloudFoundryOperations cloudFoundryOperations = createCloudFoundryOperations();
        RandomizedNameFactory randomizedNameFactory = createRandomizedNameFactory();
        createService(cloudFoundryOperations, randomizedNameFactory);
        this.cloudService = createCloudService(cloudFoundryOperations);
    }

    protected abstract T createService(CloudFoundryOperations cloudFoundryOperations, 
                                       RandomizedNameFactory randomizedNameFactory);
    
    private RandomizedNameFactory createRandomizedNameFactory() {
        RandomizedNameFactory randomizedNameFactory = mock(RandomizedNameFactory.class);
        when(randomizedNameFactory.create(serviceLabel)).thenReturn("randomized-name");
    
        return randomizedNameFactory;
    }

    private CloudFoundryOperations createCloudFoundryOperations() {
        CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

        CloudServiceOffering cloudServiceOffering = new CloudServiceOffering(null, serviceLabel);
        when(cloudFoundryOperations.getServiceOfferings()).thenReturn(Arrays.asList(cloudServiceOffering));

        return cloudFoundryOperations;
    }

    private static CloudService createCloudService(CloudFoundryOperations cloudFoundryOperations) {
        ArgumentCaptor<CloudService> cloudService = ArgumentCaptor.forClass(CloudService.class);
        verify(cloudFoundryOperations).createService(cloudService.capture());

        return cloudService.getValue();
    }

    @Test
    public void test() {
        assertEquals(serviceLabel, this.cloudService.getLabel());
        assertEquals(servicePlan, this.cloudService.getPlan());
    }
    
}