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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.Assume.assumeTrue;

@ActiveProfiles("test")
@SpringBootTest(classes = IntegrationTestConfiguration.class)
final class IntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @DisplayName("DistZip")
    @Test
    void distZip() {
        isIgnored(this.environment, "distZip");
        test(this.applicationContext.getBean(DistZipApplication.class));
    }

    @DisplayName("EJB")
    @Test
    void ejb() {
        isIgnored(this.environment, "ejb");
        test(this.applicationContext.getBean(EjbApplication.class));
    }

    @DisplayName("Groovy")
    @Test
    void groovy() {
        isIgnored(this.environment, "groovy");
        test(this.applicationContext.getBean(GroovyApplication.class));
    }

    @DisplayName("Java Main-Class")
    @Test
    void javaMain() {
        isIgnored(this.environment, "javaMain");
        test(this.applicationContext.getBean(JavaMainApplication.class));
    }

    @DisplayName("Ratpack")
    @Test
    void ratpack() {
        isIgnored(this.environment, "ratpack");
        test(this.applicationContext.getBean(RatpackApplication.class));
    }

    @DisplayName("Spring Boot CLI")
    @Test
    void springBootCli() {
        isIgnored(this.environment, "springBootCli");
        test(this.applicationContext.getBean(SpringBootCliApplication.class));
    }

    @DisplayName("Spring Boot CLI JAR")
    @Test
    void springBootCliJar() {
        isIgnored(this.environment, "springBootCliJar");
        test(this.applicationContext.getBean(SpringBootCliJarApplication.class));
    }

    @DisplayName("Web")
    @Test
    void web() {
        isIgnored(this.environment, "web");
        test(this.applicationContext.getBean(WebApplication.class));
    }

    @DisplayName("Web Servlet 2")
    @Test
    void webServlet2() {
        isIgnored(this.environment, "webServlet2");
        test(this.applicationContext.getBean(WebServlet2Application.class));
    }

    private static void isIgnored(Environment environment, String applicationType) {
        String key = String.format("test.%s", applicationType);
        assumeTrue(String.format("Test is disabled via %s", key), environment.getProperty(key, boolean.class, true));
    }

    private static void test(Application application) {
        application
            .request("/")
            .as(StepVerifier::create)
            .expectNext("ok")
            .expectComplete()
            .verify(Duration.ofMinutes(5));
    }

}
