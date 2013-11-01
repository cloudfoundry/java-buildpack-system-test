// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.io.IOException;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
public class AutoreconfigurationTest {

    private volatile Application testApp;

    @Autowired
    private volatile CloudFoundryOperations cloudFoundryOperations;

    private volatile Service mySqlService;

    @Before
    public void setup() throws IOException {
	this.mySqlService = new MySqlService(this.cloudFoundryOperations);
	this.testApp = new TestApplication(this.cloudFoundryOperations, "web-application");
	this.testApp.push().bind(this.mySqlService).start();
    }

    @After
    public void tearDown() {
	this.testApp.delete();
	this.mySqlService.delete();
    }

    @Test
    public void testAutoreconfiguration() throws IOException {
    }

}
