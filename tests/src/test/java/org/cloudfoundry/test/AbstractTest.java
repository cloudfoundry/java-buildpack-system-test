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

import org.cloudfoundry.test.support.ApplicationFactory;
import org.cloudfoundry.test.support.IgnoreOnPropertyTestRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.AsyncRestOperations;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = IntegrationTestConfiguration.class)
public abstract class AbstractTest {

    @Autowired
    @Rule
    public IgnoreOnPropertyTestRule ignoreOnPropertyTestRule;

    @Autowired
    protected ApplicationFactory applicationFactory;

    @Autowired
    protected AsyncRestOperations restOperations;

    public abstract void distZip() throws Exception;

    public abstract void ejb() throws Exception;

    public abstract void groovy() throws Exception;

    public abstract void javaMain() throws Exception;

    public abstract void ratpack() throws Exception;

    public abstract void springBootCli() throws Exception;

    public abstract void springBootCliJar() throws Exception;

    public abstract void web() throws Exception;

    public abstract void webServlet2() throws Exception;

}
