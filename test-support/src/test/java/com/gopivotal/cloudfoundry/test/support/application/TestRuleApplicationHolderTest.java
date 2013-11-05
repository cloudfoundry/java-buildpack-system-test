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

import com.gopivotal.cloudfoundry.test.support.service.Service;
import com.gopivotal.cloudfoundry.test.support.service.ServicesHolder;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class TestRuleApplicationHolderTest {

    private final Application application = mock(Application.class);

    private final ApplicationFactory applicationFactory = mock(ApplicationFactory.class);

    private final Service service = mock(Service.class);

    private final ServicesHolder servicesHolder = mock(ServicesHolder.class);

    private final Statement statement = mock(Statement.class);

    private final TestRuleApplicationHolder applicationHolder = new TestRuleApplicationHolder(this
            .applicationFactory, this.servicesHolder);

    public TestRuleApplicationHolderTest() {
        mockApplication(this.application, this.service);
        mockApplicationFactory(this.application, this.applicationFactory);
        mockServicesHolder(this.service, this.servicesHolder);
    }

    private static void mockApplication(Application application, Service service) {
        when(application.push()).thenReturn(application);
        when(application.bind(service)).thenReturn(application);
    }

    private static void mockApplicationFactory(Application application, ApplicationFactory applicationFactory) {
        when(applicationFactory.create("test-name")).thenReturn(application);
    }

    private static void mockServicesHolder(Service service, ServicesHolder servicesHolder) {
        when(servicesHolder.get()).thenReturn(new Service[]{service});
    }

    @Test
    public void noAnnotation() throws Throwable {
        doAnswer(new NoAnnotationAnswer(this.applicationHolder)).when(this.statement).evaluate();

        Description description = Description.createSuiteDescription(NoAnnotation.class);
        this.applicationHolder.apply(this.applicationHolder.apply(this.statement, description), description).evaluate();

        assertNull(this.applicationHolder.get());
        verify(this.statement).evaluate();
    }

    @Test
    public void withAnnotation() throws Throwable {
        doAnswer(new WithAnnotationAnswer(this.applicationHolder)).when(this.statement).evaluate();

        Description description = Description.createSuiteDescription(WithAnnotation.class);
        this.applicationHolder.apply(this.statement, description).evaluate();

        verify(this.application).push();
        verify(this.application).bind(this.service);
        verify(this.application).start();
        verify(this.statement).evaluate();
        verify(this.application).delete();
        assertNull(this.applicationHolder.get());
    }

    private static final class NoAnnotation {

    }

    private static final class NoAnnotationAnswer implements Answer {

        private final ApplicationHolder applicationHolder;

        private NoAnnotationAnswer(ApplicationHolder applicationHolder) {
            this.applicationHolder = applicationHolder;
        }

        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            assertNull(this.applicationHolder.get());
            return null;
        }

    }

    @CreateApplication("test-name")
    private static final class WithAnnotation {

    }

    private static final class WithAnnotationAnswer implements Answer<Void> {

        private final ApplicationHolder applicationHolder;

        private WithAnnotationAnswer(ApplicationHolder applicationHolder) {
            this.applicationHolder = applicationHolder;
        }

        @Override
        public Void answer(InvocationOnMock invocation) throws Throwable {
            Application application = this.applicationHolder.get();
            assertNotNull(application);

            return null;
        }
    }
}
