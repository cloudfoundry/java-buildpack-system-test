/*
 * Copyright 2013-2019 the original author or authors.
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
import org.cloudfoundry.test.support.application.DistZipApplication;
import org.cloudfoundry.test.support.application.EjbApplication;
import org.cloudfoundry.test.support.application.GroovyApplication;
import org.cloudfoundry.test.support.application.JavaMainApplication;
import org.cloudfoundry.test.support.application.RatpackApplication;
import org.cloudfoundry.test.support.application.SpringBootCliApplication;
import org.cloudfoundry.test.support.application.SpringBootCliJarApplication;
import org.cloudfoundry.test.support.application.WebApplication;
import org.cloudfoundry.test.support.application.WebServlet2Application;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.Assume.assumeTrue;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
public class IntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Test
    public final void distZip() {
        isIgnored(this.environment, "distZip");
        test(this.applicationContext.getBean(DistZipApplication.class));
    }

    @Test
    public final void ejb() {
        isIgnored(this.environment, "ejb");
        test(this.applicationContext.getBean(EjbApplication.class));
    }

    @Test
    public final void groovy() {
        isIgnored(this.environment, "groovy");
        test(this.applicationContext.getBean(GroovyApplication.class));
    }

    @Test
    public final void javaMain() {
        isIgnored(this.environment, "javaMain");
        test(this.applicationContext.getBean(JavaMainApplication.class));
    }

    @Test
    public final void ratpack() {
        isIgnored(this.environment, "ratpack");
        test(this.applicationContext.getBean(RatpackApplication.class));
    }

    @Test
    public final void springBootCli() {
        isIgnored(this.environment, "springBootCli");
        test(this.applicationContext.getBean(SpringBootCliApplication.class));
    }

    @Test
    public final void springBootCliJar() {
        isIgnored(this.environment, "springBootCliJar");
        test(this.applicationContext.getBean(SpringBootCliJarApplication.class));
    }

    @Test
    public final void web() {
        isIgnored(this.environment, "web");
        test(this.applicationContext.getBean(WebApplication.class));
    }

    @Test
    public final void webServlet2() {
        isIgnored(this.environment, "webServlet2");
        test(this.applicationContext.getBean(WebServlet2Application.class));
    }

    private static void isIgnored(Environment environment, String applicationType) {
        String key = String.format("test.%s", applicationType);
        assumeTrue(String.format("Test is disabled via %s", key), environment.getProperty(key, boolean.class, true));
    }

    private void test(Application application) {
        application
            .request("/")
            .as(StepVerifier::create)
            .expectNext("ok")
            .expectComplete()
            .verify(Duration.ofMinutes(5));
    }

}
