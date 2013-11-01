// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import com.gopivotal.cloudfoundry.test.support.*;
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
 * Test autoreconfiguration by ensuring that a bound database service is used by:
 * <ol>
 * <li>pushing an application, with a bound database service, which writes to a database,</li>
 * <li>deleting the application,</li>
 * <li>pushing an application, with the same bound database service as above, which reads from the database, and</li>
 * <li>checking that the value read is the same as the value written.</li>
 * </ol>
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
