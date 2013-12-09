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
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ElephantSqlServiceTest {

    private final CloudService cloudService;

    public ElephantSqlServiceTest() {
        CloudFoundryOperations cloudFoundryOperations = createCloudFoundryOperations();
        RandomizedNameFactory randomizedNameFactory = createRandomizedNameFactory();
        createService(cloudFoundryOperations, randomizedNameFactory);
        this.cloudService = createCloudService(cloudFoundryOperations);
    }

    private static CloudFoundryOperations createCloudFoundryOperations() {
        CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

        CloudServiceOffering cloudServiceOffering = new CloudServiceOffering(null, "elephantsql");
        when(cloudFoundryOperations.getServiceOfferings()).thenReturn(Arrays.asList(cloudServiceOffering));

        return cloudFoundryOperations;
    }

    private static RandomizedNameFactory createRandomizedNameFactory() {
        RandomizedNameFactory randomizedNameFactory = mock(RandomizedNameFactory.class);
        when(randomizedNameFactory.create("elephantsql")).thenReturn("randomized-name");

        return randomizedNameFactory;
    }

    private static ElephantSqlService createService(CloudFoundryOperations cloudFoundryOperations,
                                                    RandomizedNameFactory randomizedNameFactory) {
        return new ElephantSqlService(cloudFoundryOperations, randomizedNameFactory);
    }

    private static CloudService createCloudService(CloudFoundryOperations cloudFoundryOperations) {
        ArgumentCaptor<CloudService> cloudService = ArgumentCaptor.forClass(CloudService.class);
        verify(cloudFoundryOperations).createService(cloudService.capture());

        return cloudService.getValue();
    }

    @Test
    public void test() {
        assertEquals("elephantsql", this.cloudService.getLabel());
        assertEquals("turtle", this.cloudService.getPlan());
    }

}
