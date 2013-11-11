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
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URI;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

public final class TestRuleServicesHolderTest {

    private final CloudFoundryOperations cloudFoundryOperations = mock(CloudFoundryOperations.class);

    private final RandomizedNameFactory randomizedNameFactory = mock(RandomizedNameFactory.class);

    private final Statement statement = mock(Statement.class);

    private final TestRuleServicesHolder testRuleServicesHolder = new TestRuleServicesHolder(this.cloudFoundryOperations, this.randomizedNameFactory);

    @Test
    public void noAnnotation() throws Throwable {
        doAnswer(new NoAnnotationRule(this.testRuleServicesHolder)).when(this.statement).evaluate();

        Description description = Description.createSuiteDescription(NoAnnotation.class);
        this.testRuleServicesHolder.apply(this.statement, description).evaluate();

        assertEquals(0, this.testRuleServicesHolder.get().length);
        verify(this.statement).evaluate();
    }

    @Test
    public void withAnnotation() throws Throwable {
        final StubService[] service = new StubService[1];

        doAnswer(new WithAnnotationAnswer(service, this.testRuleServicesHolder)).when(this.statement).evaluate();

        Description description = Description.createSuiteDescription(WithAnnotation.class);
        this.testRuleServicesHolder.apply(this.statement, description).evaluate();

        assertEquals(0, this.testRuleServicesHolder.get().length);
        verify(this.statement).evaluate();
        assertTrue(service[0].deleteCalled);
    }

    private static final class NoAnnotation {

    }

    private static final class NoAnnotationRule implements Answer {

        private final ServicesHolder servicesHolder;

        private NoAnnotationRule(ServicesHolder servicesHolder) {
            this.servicesHolder = servicesHolder;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            assertEquals(0, this.servicesHolder.get().length);
            return null;
        }

    }

    @CreateServices(StubService.class)
    private static final class WithAnnotation {

    }

    private static final class StubService implements Service {

        private volatile boolean deleteCalled = false;

        @SuppressWarnings("unused")
        StubService(CloudFoundryOperations cloudFoundryOperations, RandomizedNameFactory randomizedNameFactory) {
        }

        @Override
        public String getName() {
            return "stub-service";
        }

        @Override
        public URI getEndpoint(Map<String, String> environmentVariables) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            this.deleteCalled = true;
        }

    }

    private static final class WithAnnotationAnswer implements Answer<Void> {

        private final StubService[] service;

        private final ServicesHolder servicesHolder;

        public WithAnnotationAnswer(StubService[] service, ServicesHolder servicesHolder) {
            this.service = service;
            this.servicesHolder = servicesHolder;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            Service[] services = this.servicesHolder.get();
            assertEquals(1, services.length);

            this.service[0] = (StubService) services[0];
            return null;
        }

    }
}
