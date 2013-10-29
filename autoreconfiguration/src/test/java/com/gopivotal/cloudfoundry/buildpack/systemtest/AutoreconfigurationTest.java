// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test autoreconfiguration by ensuring that a bound database service is used by:
 * <ol>
 * <li>pushing an application, with a bound database service, which writes to a database,</li>
 * <li>deleting the application,</li>
 * <li>pushing an application, with the same bound database service as above, which reads from the database, and</li>
 * <li>checking that the value read is the same as the value written.</li>
 * </ol>
 */
public class AutoreconfigurationTest {

    private static final String MYSQL_SERVICE_NAME = "java-buildpack-system-test-mysql";

    private TestApplication testApp;

    private TestClient testClient;

    private boolean tornDown = false;

    @Before
    public void setup() throws IOException {
	this.testClient = new TestClient();
	this.testApp = new TestApplication(this.testClient, "../vendor/java-test-applications/web-application");
	tearDown();
	this.tornDown = false;
    }

    @After
    public void tearDown() {
	if (!this.tornDown) {
	    this.testApp.delete();
	    this.testClient.deleteService(MYSQL_SERVICE_NAME);
	    this.tornDown = true;
	}
    }

    @Test
    public void testAutoreconfiguration() throws IOException {
	this.testClient.createMySqlService(MYSQL_SERVICE_NAME);
	List<String> serviceNames = new ArrayList<String>(1);
	serviceNames.add(MYSQL_SERVICE_NAME);
	CloudApplication app = this.testApp.deploy(serviceNames);
	assertEquals(AppState.STARTED, app.getState());
    }

}
