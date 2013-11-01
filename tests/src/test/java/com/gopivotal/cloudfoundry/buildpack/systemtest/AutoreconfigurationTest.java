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

package com.gopivotal.cloudfoundry.buildpack.systemtest;

import com.gopivotal.cloudfoundry.test.support.Application;
import com.gopivotal.cloudfoundry.test.support.CloudFoundryApplication;
import com.gopivotal.cloudfoundry.test.support.MySqlService;
import com.gopivotal.cloudfoundry.test.support.Service;
import com.gopivotal.cloudfoundry.test.support.TestConfiguration;
import com.gopivotal.cloudfoundry.test.support.util.IoUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * Test autoreconfiguration by ensuring that a bound database service is used by: <ol> <li>pushing an application, with
 * a bound database service, which writes to a database,</li> <li>deleting the application,</li> <li>pushing an
 * application, with the same bound database service as above, which reads from the database, and</li> <li>checking that
 * the value read is the same as the value written.</li> </ol>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public final class AutoreconfigurationTest {

    private volatile Application testApp;

    @Autowired
    private volatile CloudFoundryOperations cloudFoundryOperations;

    private volatile Service mySqlService;

    @Before
    public void setup() throws IOException {
        this.mySqlService = new MySqlService(this.cloudFoundryOperations);
        this.testApp = new CloudFoundryApplication(this.cloudFoundryOperations, "web-application");
        this.testApp.push().bind(this.mySqlService).start();
    }

    @After
    public void tearDown() {
        IoUtils.deleteQuietly(this.testApp, this.mySqlService);
    }

    @Test
    public void testAutoreconfiguration() throws IOException {
    }

}
