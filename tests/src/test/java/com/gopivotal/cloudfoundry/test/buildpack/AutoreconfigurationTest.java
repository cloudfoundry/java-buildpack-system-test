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

package com.gopivotal.cloudfoundry.test.buildpack;

import com.gopivotal.cloudfoundry.test.support.TestConfiguration;
import com.gopivotal.cloudfoundry.test.support.application.Application;
import com.gopivotal.cloudfoundry.test.support.application.CloudFoundryApplication;
import com.gopivotal.cloudfoundry.test.support.service.ClearDbService;
import com.gopivotal.cloudfoundry.test.support.service.Service;
import com.gopivotal.cloudfoundry.test.support.util.IoUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.initializer.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public final class AutoReconfigurationTest {

    @Autowired
    private volatile CloudFoundryOperations cloudFoundryOperations;

    private volatile Service clearDbService;

    private volatile Application application;

    @Before
    public void setup() throws IOException {
        this.clearDbService = new ClearDbService(this.cloudFoundryOperations);
        this.application = new CloudFoundryApplication(this.cloudFoundryOperations, "web-application");
        this.application.push().bind(this.clearDbService).start();
    }

    @After
    public void tearDown() {
        IoUtils.deleteQuietly(this.application, this.clearDbService);
    }

    @Test
    public void autoReconfiguration() throws IOException {
        System.out.println(this.application.getTestOperations().classPath());
    }

}
