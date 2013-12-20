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

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.Test;
import org.junit.runners.model.Statement;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ApplicationCleanupTestRuleTest {

    private final CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

    private final RandomizedNameFactory randomizedNameFactory = mock(RandomizedNameFactory.class);

    private final Statement statement = mock(Statement.class);

    private final ApplicationCleanupTestRule testRule = new ApplicationCleanupTestRule(this.cloudFoundryOperations,
            "test.domain", this.randomizedNameFactory);

    @Test
    public void deleteResidualApplication() throws Throwable {
        when(this.cloudFoundryOperations.getApplications()).thenReturn(Arrays.asList(new CloudApplication(null,
                "test-application-name")));
        when(this.randomizedNameFactory.matches("test-application-name")).thenReturn(true);

        this.testRule.apply(this.statement, null).evaluate();

        verify(this.cloudFoundryOperations).deleteRoute("test-application-name", "test.domain");
        verify(this.cloudFoundryOperations).deleteApplication("test-application-name");
    }

    @Test
    public void doNotDeleteNonOwnedApplication() throws Throwable {
        when(this.cloudFoundryOperations.getApplications()).thenReturn(Arrays.asList(new CloudApplication(null,
                "test-application-name")));
        when(this.randomizedNameFactory.matches("test-application-name")).thenReturn(false);

        this.testRule.apply(this.statement, null).evaluate();

        verify(this.cloudFoundryOperations, times(0)).deleteRoute("test-application-name", "test.domain");
        verify(this.cloudFoundryOperations, times(0)).deleteApplication("test-application-name");
    }

    @Test
    public void onlyRunOnce() throws Throwable {
        when(this.cloudFoundryOperations.getApplications()).thenReturn(Arrays.asList(new CloudApplication(null,
                "test-application-name")));
        when(this.randomizedNameFactory.matches("test-application-name")).thenReturn(true);

        this.testRule.apply(this.statement, null).evaluate();
        this.testRule.apply(this.statement, null).evaluate();

        verify(this.cloudFoundryOperations, times(1)).deleteRoute("test-application-name", "test.domain");
        verify(this.cloudFoundryOperations, times(1)).deleteApplication("test-application-name");
    }
}

