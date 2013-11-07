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

package com.gopivotal.cloudfoundry.test.support.runner;

import com.gopivotal.cloudfoundry.test.support.application.Application;
import com.gopivotal.cloudfoundry.test.support.application.ApplicationFactory;
import com.gopivotal.cloudfoundry.test.support.service.Service;
import com.gopivotal.cloudfoundry.test.support.service.ServicesHolder;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MethodInvokerTest {

    private final Application application = mock(Application.class);

    private final TestContext testContext = mock(TestContext.class);

    private final MethodInvoker methodInvoker;

    private final Service service = mock(Service.class);

    private final Stub stub = new Stub();

    public MethodInvokerTest() throws NoSuchMethodException {
        mockApplication(this.application, this.service);
        ApplicationFactory applicationFactory = createApplicationFactory(this.application);
        ServicesHolder servicesHolder = createServicesHolder(this.service);
        ApplicationContext applicationContext = createApplicationContext(applicationFactory, servicesHolder);
        mockTestContext(applicationContext, this.testContext);

        this.methodInvoker = new MethodInvoker(new FrameworkMethod(Stub.class.getMethod("setApplication",
                Application.class)), this.stub, "test-name");
    }

    private static void mockApplication(Application application, Service service) {
        when(application.bind(service)).thenReturn(application);
        when(application.push()).thenReturn(application);
        when(application.start()).thenReturn(application);
    }

    private static ApplicationContext createApplicationContext(ApplicationFactory applicationFactory,
                                                               ServicesHolder servicesHolder) {
        ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ApplicationFactory.class)).thenReturn(applicationFactory);
        when(applicationContext.getBean(ServicesHolder.class)).thenReturn(servicesHolder);

        return applicationContext;
    }

    private static ApplicationFactory createApplicationFactory(Application application) {
        ApplicationFactory applicationFactory = mock(ApplicationFactory.class);
        when(applicationFactory.create("test-name")).thenReturn(application);

        return applicationFactory;
    }

    private static ServicesHolder createServicesHolder(Service service) {
        ServicesHolder servicesHolder = mock(ServicesHolder.class);
        when(servicesHolder.get()).thenReturn(new Service[]{service});

        return servicesHolder;
    }

    private static void mockTestContext(ApplicationContext applicationContext, TestContext testContext) {
        when(testContext.getApplicationContext()).thenReturn(applicationContext);
    }

    @Test
    public void evaluate() throws Throwable {
        this.methodInvoker.beforeTestMethod(this.testContext);

        this.methodInvoker.evaluate();

        verify(this.application).push();
        verify(this.application).bind(this.service);
        verify(this.application).start();
        verify(this.application).delete();

        assertSame(this.application, this.stub.application);
    }

    @Test
    public void beforeTestClass() throws Exception {
        this.methodInvoker.beforeTestClass(this.testContext);
    }

    @Test
    public void prepareTestInstance() throws Exception {
        this.methodInvoker.prepareTestInstance(this.testContext);
    }

    @Test
    public void afterTestMethod() throws Exception {
        this.methodInvoker.afterTestMethod(this.testContext);
    }

    @Test
    public void afterTestClass() throws Exception {
        this.methodInvoker.afterTestClass(this.testContext);
    }

    /**
     * A stub class for testing
     */
    public static final class Stub {

        private volatile Application application;

        /**
         * Sets the application
         *
         * @param application the application
         */
        public void setApplication(Application application) {
            this.application = application;
        }
    }

}
