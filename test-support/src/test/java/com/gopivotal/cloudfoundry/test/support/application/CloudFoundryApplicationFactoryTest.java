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
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class CloudFoundryApplicationFactoryTest {

    private final CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

    private final ManifestFactory manifestFactory = mock(ManifestFactory.class);

    private final RandomizedNameFactory randomizedNameFactory = mock(RandomizedNameFactory.class);

    private final TestOperationsFactory testOperationsFactory = mock(TestOperationsFactory.class);

    private final CloudFoundryApplicationFactory applicationFactory = new CloudFoundryApplicationFactory
            (this.cloudFoundryOperations, this.manifestFactory, this.randomizedNameFactory,
                    this.testOperationsFactory);

    public CloudFoundryApplicationFactoryTest() {
        mockCloudFoundryOperations(this.cloudFoundryOperations);
        mockManifestFactory(this.manifestFactory);
    }

    private static void mockCloudFoundryOperations(CloudFoundryOperations cloudFoundryOperations) {
        CloudDomain ownedDomain = new CloudDomain(null, "owned.domain", new CloudOrganization(null, "test-org"));
        CloudDomain defaultDomain = new CloudDomain(null, "test.domain", new CloudOrganization(null, "none"));
        when(cloudFoundryOperations.getDomainsForOrg()).thenReturn(Arrays.asList(ownedDomain, defaultDomain));
    }

    private static void mockManifestFactory(ManifestFactory manifestFactory) {
        Manifest manifest = mock(Manifest.class);
        when(manifest.getBuildpack()).thenReturn("test-buildpack");
        when(manifest.getInstances()).thenReturn(8);
        when(manifest.getMemory()).thenReturn(512);
        when(manifest.getName()).thenReturn("test-application-name");
        when(manifest.getPath()).thenReturn(new File("test-path"));

        when(manifestFactory.create(new File("../vendor/java-test-applications/test-name"))).thenReturn(manifest);
    }

    @Test
    public void create() throws Exception {
        assertTrue(this.applicationFactory.create("test-name") instanceof CloudFoundryApplication);
    }
}
