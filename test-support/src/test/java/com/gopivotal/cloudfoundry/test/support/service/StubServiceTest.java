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

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class StubServiceTest {

    private final CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

    @Test(expected = IllegalArgumentException.class)
    public void noServiceOffering() {
        when(this.cloudFoundryOperations.getServiceOfferings()).thenReturn(Arrays.<CloudServiceOffering>asList());
        RandomizedNameFactory randomizedNameFactory = createRandomizedNameFactory();
        createService(this.cloudFoundryOperations, randomizedNameFactory);
    }

    @Test
    public void preferDevServices() {
        ArgumentCaptor<CloudService> cloudServiceCaptor = ArgumentCaptor.forClass(CloudService.class);
        when(this.cloudFoundryOperations.getServiceOfferings()).thenReturn(Arrays.asList(
                new CloudServiceOffering(null, "test-label"), new CloudServiceOffering(null, "test-label-dev")));
        RandomizedNameFactory randomizedNameFactory = createRandomizedNameFactory();

        new StubService(this.cloudFoundryOperations, "test-label", "test-plan", randomizedNameFactory);

        verify(this.cloudFoundryOperations).createService(cloudServiceCaptor.capture());
        assertEquals("test-label-dev", cloudServiceCaptor.getValue().getLabel());
    }

    @Test
    public void fallbackToNonDevServices() {
        ArgumentCaptor<CloudService> cloudServiceCaptor = ArgumentCaptor.forClass(CloudService.class);
        when(this.cloudFoundryOperations.getServiceOfferings()).thenReturn(Arrays.asList(
                new CloudServiceOffering(null, "test-label")));
        RandomizedNameFactory randomizedNameFactory = createRandomizedNameFactory();

        new StubService(this.cloudFoundryOperations, "test-label", "test-plan", randomizedNameFactory);

        verify(this.cloudFoundryOperations).createService(cloudServiceCaptor.capture());
        assertEquals("test-label", cloudServiceCaptor.getValue().getLabel());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("randomized-name", getService().getName());
    }

    @Test
    public void delete() throws Exception {
        getService().delete();

        verify(this.cloudFoundryOperations).deleteService("randomized-name");
    }

    @Test
    public void getCredentials() throws Exception {
        Map<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put("VCAP_SERVICES", "{\"service-n/a\":[{\"name\":\"randomized-name\"," +
                "\"label\":\"service-n/a\",\"tags\":[\"test-tag\"],\"plan\":\"test-plan\"," +
                "\"credentials\":{\"type\":\"value\"}}]}");

        Map<String, ?> credentials = getService().getCredentials(environmentVariables);
        assertTrue(credentials.containsKey("type"));
    }

    @Test(expected = IllegalStateException.class)
    public void getCredentialsNoService() {
        Map<String, String> environmentVariables = new HashMap<>();
        environmentVariables.put("VCAP_SERVICES", "{\"service-n/a\":[{\"name\":\"test-name\"," +
                "\"label\":\"service-n/a\",\"tags\":[\"test-tag\"],\"plan\":\"test-plan\"," +
                "\"credentials\":{\"type\":\"value\"}}]}");

        getService().getCredentials(environmentVariables);
    }

    private AbstractService getService() {
        mockCloudFoundryOperations(this.cloudFoundryOperations);
        RandomizedNameFactory randomizedNameFactory = createRandomizedNameFactory();
        return createService(this.cloudFoundryOperations, randomizedNameFactory);
    }

    private void mockCloudFoundryOperations(CloudFoundryOperations cloudFoundryOperations) {
        CloudServiceOffering cloudServiceOffering1 = new CloudServiceOffering(null, "other-label");
        CloudServiceOffering cloudServiceOffering2 = new CloudServiceOffering(null, "test-label-dev");
        when(cloudFoundryOperations.getServiceOfferings()).thenReturn(Arrays.asList(cloudServiceOffering1,
                cloudServiceOffering2));
    }

    private RandomizedNameFactory createRandomizedNameFactory() {
        RandomizedNameFactory randomizedNameFactory = mock(RandomizedNameFactory.class);
        when(randomizedNameFactory.create("test-label")).thenReturn("randomized-name");

        return randomizedNameFactory;
    }

    private static StubService createService(CloudFoundryOperations cloudFoundryOperations,
                                             RandomizedNameFactory randomizedNameFactory) {
        StubService service = new StubService(cloudFoundryOperations, "test-label", "test-plan", randomizedNameFactory);

        ArgumentCaptor<CloudService> cloudServiceCaptor = ArgumentCaptor.forClass(CloudService.class);
        verify(cloudFoundryOperations).createService(cloudServiceCaptor.capture());

        CloudService cloudService = cloudServiceCaptor.getValue();
        assertEquals("randomized-name", cloudService.getName());
        assertEquals("core", cloudService.getProvider());
        assertEquals("test-label-dev", cloudService.getLabel());
        assertNull(cloudService.getVersion());
        assertEquals("test-plan", cloudService.getPlan());

        return service;
    }

    private static final class StubService extends AbstractService {

        private StubService(CloudFoundryOperations cloudFoundryOperations, String label, String plan,
                            RandomizedNameFactory randomizedNameFactory) {
            super(cloudFoundryOperations, label, plan, randomizedNameFactory);
        }

        @Override
        public URI getEndpoint(Map<String, String> environmentVariables) {
            throw new UnsupportedOperationException();
        }
    }
}