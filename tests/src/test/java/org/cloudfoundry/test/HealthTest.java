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


import org.cloudfoundry.test.support.Application;
import org.cloudfoundry.test.support.IgnoreOnProperty;
import org.cloudfoundry.util.test.TestSubscriber;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

public final class HealthTest extends AbstractTest {

    @Test
    @IgnoreOnProperty("test.health.dist-zip")
    @Override
    public void distZip() throws Exception {
        test("dist-zip");
    }

    @Test
    @IgnoreOnProperty("test.health.ejb")
    @Override
    public void ejb() throws Exception {
        test("ejb");
    }

    @Test
    @IgnoreOnProperty("test.health.groovy")
    @Override
    public void groovy() throws Exception {
        test("groovy");
    }

    @Test
    @IgnoreOnProperty("test.health.java-main")
    @Override
    public void javaMain() throws Exception {
        test("java-main");
    }

    @Test
    @IgnoreOnProperty("test.health.ratpack")
    @Override
    public void ratpack() throws Exception {
        test("ratpack");
    }

    @Test
    @IgnoreOnProperty("test.health.spring-boot-cli")
    @Override
    public void springBootCli() throws Exception {
        test("spring-boot-cli");
    }

    @Test
    @IgnoreOnProperty("test.health.spring-boot-cli-jar")
    @Override
    public void springBootCliJar() throws Exception {
        test("spring-boot-cli-jar");
    }

    @Test
    @IgnoreOnProperty("test.health.web")
    @Override
    public void web() throws Exception {
        test("web");
    }

    @Test
    @IgnoreOnProperty("test.health.web-servlet-2")
    @Override
    public void webServlet2() throws Exception {
        test("web-servlet-2");
    }

    private Mono<String> request(String host) {
        return Mono
            .fromFuture(this.restOperations.getForEntity("http://{host}", String.class, host))
            .map(HttpEntity::getBody);
    }

    private void test(String type) throws Exception {
        try (Application application = this.applicationFactory.get(type)) {
            TestSubscriber<String> testSubscriber = new TestSubscriber<>();

            application.push()
                .after(application::start)
                .after(() -> application.host()
                    .then(this::request))
                .subscribe(testSubscriber
                    .assertEquals("ok"));

            testSubscriber.verify(5, TimeUnit.MINUTES);
        }
    }

}
