/*
 * Copyright 2016 the original author or authors.
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

package org.cloudfoundry.test;

import org.cloudfoundry.test.support.application.Application;
import org.cloudfoundry.test.support.application.ApplicationDirectory;
import org.cloudfoundry.test.support.application.DistZipApplication;
import org.cloudfoundry.test.support.application.EjbApplication;
import org.cloudfoundry.test.support.application.GroovyApplication;
import org.cloudfoundry.test.support.application.JavaMainApplication;
import org.cloudfoundry.test.support.application.RatpackApplication;
import org.cloudfoundry.test.support.application.SpringBootCliApplication;
import org.cloudfoundry.test.support.application.SpringBootCliJarApplication;
import org.cloudfoundry.test.support.application.WebApplication;
import org.cloudfoundry.test.support.application.WebServlet2Application;
import org.cloudfoundry.test.support.service.Service;
import org.cloudfoundry.util.test.TestSubscriber;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Duration;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assume.assumeTrue;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestConfiguration.class)
public abstract class AbstractTest<T> {

    private final String testType;

    @Autowired
    private ApplicationDirectory applicationDirectory;

    @Autowired
    private Environment environment;

    protected AbstractTest(String testType) {
        this.testType = testType;
    }

    @Test
    public final void distZip() throws InterruptedException {
        isIgnored(this.environment, this.testType, "distZip");
        test(this.applicationDirectory.get(DistZipApplication.class));
    }

    @Test
    public final void ejb() throws InterruptedException {
        isIgnored(this.environment, this.testType, "ejb");
        test(this.applicationDirectory.get(EjbApplication.class));
    }

    @Test
    public final void groovy() throws InterruptedException {
        isIgnored(this.environment, this.testType, "groovy");
        test(this.applicationDirectory.get(GroovyApplication.class));
    }

    @Test
    public final void javaMain() throws InterruptedException {
        isIgnored(this.environment, this.testType, "javaMain");
        test(this.applicationDirectory.get(JavaMainApplication.class));
    }

    @Test
    public final void ratpack() throws InterruptedException {
        isIgnored(this.environment, this.testType, "ratpack");
        test(this.applicationDirectory.get(RatpackApplication.class));
    }

    @Test
    public final void springBootCli() throws InterruptedException {
        isIgnored(this.environment, this.testType, "springBootCli");
        test(this.applicationDirectory.get(SpringBootCliApplication.class));
    }

    @Test
    public final void springBootCliJar() throws InterruptedException {
        isIgnored(this.environment, this.testType, "springBootCliJar");
        test(this.applicationDirectory.get(SpringBootCliJarApplication.class));
    }

    @Test
    public final void web() throws InterruptedException {
        isIgnored(this.environment, this.testType, "web");
        test(this.applicationDirectory.get(WebApplication.class));
    }

    @Test
    public final void webServlet2() throws InterruptedException {
        isIgnored(this.environment, this.testType, "webServlet2");
        test(this.applicationDirectory.get(WebServlet2Application.class));
    }

    protected Service getService() {
        return null;
    }

    protected abstract void test(Application application, TestSubscriber<T> testSubscriber);

    private static void isIgnored(Environment environment, String testType, String applicationType) {
        String key = String.format("test.%s.%s", testType, applicationType);
        assumeTrue(String.format("Test is disabled via %s", key), environment.getProperty(key, boolean.class, true));
    }

    private void test(Application application) throws InterruptedException {
        Optional<Service> service = Optional.ofNullable(getService());

        try {
            service.ifPresent(s -> application.bindService(s).get(Duration.ofMinutes(1)));

            TestSubscriber<T> testSubscriber = new TestSubscriber<>();
            test(application, testSubscriber);
            testSubscriber.verify(5, MINUTES);
        } finally {
            service.ifPresent(s -> application.unbindService(s).get(Duration.ofMinutes(1)));
        }
    }

}
