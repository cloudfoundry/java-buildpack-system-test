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
import org.cloudfoundry.client.lib.domain.Staging;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class CloudFoundryApplicationTest {

    private final CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

    private final Manifest manifest = mock(Manifest.class);

    private final TestOperationsFactory testOperationsFactory = mock(TestOperationsFactory.class);

    private final CloudFoundryApplication application;

    public CloudFoundryApplicationTest() {
        mockManifest(this.manifest);
        mockTestOperationsFactory(this.testOperationsFactory);
        this.application = createApplication(this.cloudFoundryOperations, this.manifest, this.testOperationsFactory);
    }

    private static void mockManifest(Manifest manifest) {
        when(manifest.getBuildpack()).thenReturn("test-buildpack");
        when(manifest.getInstances()).thenReturn(8);
        when(manifest.getMemory()).thenReturn(512);
        when(manifest.getName()).thenReturn("test-application-name");
        when(manifest.getPath()).thenReturn(new File("test-path"));
    }

    private static void mockTestOperationsFactory(TestOperationsFactory testOperationsFactory) {
        TestOperations testOperations = mock(TestOperations.class);
        when(testOperationsFactory.create("test-name.test.domain")).thenReturn(testOperations);
    }

    @SuppressWarnings("unchecked")
    private static CloudFoundryApplication createApplication(CloudFoundryOperations cloudFoundryOperations,
                                                             Manifest manifest,
                                                             TestOperationsFactory testOperationsFactory) {
        CloudFoundryApplication application = new CloudFoundryApplication(cloudFoundryOperations, "test.domain",
                manifest, "test-name", testOperationsFactory);

        ArgumentCaptor<Staging> staging = ArgumentCaptor.forClass(Staging.class);
        ArgumentCaptor<List> uris = ArgumentCaptor.forClass(List.class);
        verify(cloudFoundryOperations).createApplication(eq("test-name"), staging.capture(), eq(512),
                uris.capture(), eq(Collections.<String>emptyList()));

        assertNull(staging.getValue().getCommand());
        assertEquals("test-buildpack", staging.getValue().getBuildpackUrl());
        assertEquals("test-name.test.domain", uris.getValue().get(0));

        return application;
    }

    @Test
    public void bind() {
        Service service = mock(Service.class);
        when(service.getName()).thenReturn("service-name-1", "service-name-2");

        this.application.bind(service, service);

        verify(this.cloudFoundryOperations).bindService("test-name", "service-name-1");
        verify(this.cloudFoundryOperations).bindService("test-name", "service-name-2");
    }

    @Test
    public void deleteApplicationExists() {
        CloudApplication cloudApplication1 = new CloudApplication(null, "test-application-name");
        CloudApplication cloudApplication2 = new CloudApplication(null, this.application.getName());
        when(this.cloudFoundryOperations.getApplications()).thenReturn(Arrays.asList(cloudApplication1,
                cloudApplication2));

        this.application.delete();

        verify(this.cloudFoundryOperations).deleteApplication("test-name");
    }

    @Test
    public void deleteApplicationDoesNotExist() {
        when(this.cloudFoundryOperations.getApplications()).thenReturn(Collections.<CloudApplication>emptyList());

        this.application.delete();

        verify(this.cloudFoundryOperations, never()).deleteApplication(anyString());
    }

    @Test
    public void getTestOperations() {
        assertNotNull(this.application.getTestOperations());
    }

    @Test
    public void push() throws IOException {
        this.application.push();

        verify(this.cloudFoundryOperations).uploadApplication("test-name", new File("test-path"));
    }

    @Test(expected = RuntimeException.class)
    public void pushWithIoException() throws IOException {
        doThrow(new IOException()).when(this.cloudFoundryOperations).uploadApplication("test-name",
                new File("test-path"));

        this.application.push();
    }

    @Test
    public void start() {
        this.application.start();

        verify(this.cloudFoundryOperations).startApplication("test-name");
    }

    @Test
    public void stop() {
        this.application.stop();

        verify(this.cloudFoundryOperations).stopApplication("test-name");
    }

    @Test
    public void unbind() {
        Service service = mock(Service.class);
        when(service.getName()).thenReturn("service-name-1", "service-name-2");

        this.application.unbind(service, service);

        verify(this.cloudFoundryOperations).unbindService("test-name", "service-name-1");
        verify(this.cloudFoundryOperations).unbindService("test-name", "service-name-2");
    }

}
