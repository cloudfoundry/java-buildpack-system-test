// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.Staging;

/**
 * {@link TestClient} provides a convenient abstraction for pushing Java test applications to Cloud Foundry.
 */
public class TestClient {

    public static final int DEFAULT_MEMORY_LIMIT_MEGABYTES = 1024;

    private final CloudFoundryClient cfClient;

    private final String defaultDomainName;

    private static final String JAVA_BUILDPACK_URL = "https://github.com/cloudfoundry/java-buildpack.git";

    private static final String MYSQL_PLAN = "spark";

    private static final String MY_SQL_LABEL = "cleardb";

    /**
     * Constructs a {@link TestClient} instance based on the following environment variable values:
     * <ul>
     * <li><code>CF_ORG</code> - The Cloud Foundry organisation to use.</li>
     * <li><code>CF_PASSWORD</code> - The password corresponding to the Cloud Foundry username.</li>
     * <li><code>CF_SPACE</code> - The Cloud Foundry space to use.</li>
     * <li><code>CF_TARGET</code> - The Cloud Foundry instance to use, e.g. <code>https://api.run.pivotal.io</code>.</li>
     * <li><code>CF_USERNAME</code> - A Cloud Foundry username, which often consists of an email address.</li>
     * </ul>
     */
    public TestClient() {
	String org_name = getEnvVariableValue("CF_ORG");
	String password = getEnvVariableValue("CF_PASSWORD");
	String space_name = getEnvVariableValue("CF_SPACE");
	String target = getEnvVariableValue("CF_TARGET");
	String user = getEnvVariableValue("CF_USERNAME");

	CloudCredentials credentials = new CloudCredentials(user, password);
	this.cfClient = new CloudFoundryClient(credentials, getTargetURL(target), org_name, space_name);
	this.cfClient.login();

	this.defaultDomainName = getDefaultDomain(this.cfClient.getDomainsForOrg()).getName();
    }

    private String getEnvVariableValue(String envVarName) {
	Map<String, String> env = System.getenv();
	String value = env.get(envVarName);
	if (value == null) {
	    throw new IllegalArgumentException("Environment variable '" + envVarName + "' must be defined");
	}
	return value;
    }

    private CloudDomain getDefaultDomain(List<CloudDomain> domains) {
	for (CloudDomain domain : domains) {
	    if (domain.getOwner().getName().equals("none")) {
		return domain;
	    }
	}
	return null;
    }

    private static URL getTargetURL(String target) {
	try {
	    return new URI(target).toURL();
	} catch (Exception e) {
	    fail("the target URL is not valid: " + e.getMessage());
	}
	return null;
    }

    private String computeAppUri(String subdomain) {
	return subdomain + "." + this.defaultDomainName;
    }

    private boolean applicationExists(String appName) {
	boolean found = false;
	List<CloudApplication> apps = this.cfClient.getApplications();
	app_loop: for (CloudApplication app : apps) {
	    if (appName.equals(app.getName())) {
		found = true;
		break app_loop;
	    }
	}
	return found;
    }

    /**
     * Pushes a Java application to Cloud Foundry, with the default memory limit, and starts the application.
     * 
     * @param applicationName the application name
     * @param subdomain the application-specific portion of the application's URL
     * @param application a {@link File} containing the application on the file system
     * @param serviceNames the names of any services to be bound to the application
     * @return a {@link CloudApplication} representing the pushed application
     * @throws IOException if uploading the application fails
     */
    public CloudApplication deployApplication(String applicationName, String subdomain, File application,
	    List<String> serviceNames) throws IOException {
	return deployApplication(applicationName, subdomain, application, serviceNames, DEFAULT_MEMORY_LIMIT_MEGABYTES);
    }

    /**
     * Pushes a Java application to Cloud Foundry and starts the application.
     * 
     * @param applicationName the application name subdomain
     * @param application a {@link File} containing the application on the file system
     * @param serviceNames the names of any services to be bound to the application
     * @param memoryLimit the memory limit of the application
     * @return a {@link CloudApplication} representing the pushed application
     * @throws IOException if uploading the application fails
     */
    public CloudApplication deployApplication(String applicationName, String subdomain, File application,
	    List<String> serviceNames, int memoryLimit) throws IOException {
	List<String> uris = new ArrayList<String>(1);
	uris.add(computeAppUri(subdomain));
	Staging staging = new Staging(null, JAVA_BUILDPACK_URL);
	this.cfClient.createApplication(applicationName, staging, memoryLimit, uris, serviceNames);
	this.cfClient.uploadApplication(applicationName, application.getCanonicalPath());

	this.cfClient.startApplication(applicationName);
	CloudApplication app = this.cfClient.getApplication(applicationName);
	return app;
    }

    /**
     * Deletes the application with the given name if and only if such an application exists.
     * 
     * @param applicationName the name of the application to be deleted
     * @return <code>true</code> if and only if the application existed and was deleted
     */
    public boolean deleteApplication(String applicationName) {
	boolean deleted = false;
	if (applicationExists(applicationName)) {
	    this.cfClient.deleteApplication(applicationName);
	    deleted = true;
	}
	return deleted;
    }

    /**
     * Creates a MySQL service with the given name.
     * 
     * @param serviceName the name of the MySQL service to be created
     * @return the {@link CloudService} created
     */
    public CloudService createMySqlService(String serviceName) {
	System.out.println(String.format("Creating MySQL service named '%s'", serviceName));
	List<CloudServiceOffering> serviceOfferings = this.cfClient.getServiceOfferings();
	CloudServiceOffering databaseServiceOffering = null;
	for (CloudServiceOffering so : serviceOfferings) {
	    if (so.getLabel().equals(MY_SQL_LABEL)) {
		databaseServiceOffering = so;
		break;
	    }
	}
	if (databaseServiceOffering == null) {
	    throw new IllegalStateException("No CloudServiceOffering found for MySQL.");
	}
	CloudService service = new CloudService(CloudEntity.Meta.defaultMeta(), serviceName);
	service.setProvider("core");
	service.setLabel(MY_SQL_LABEL);
	service.setVersion(databaseServiceOffering.getVersion());
	service.setPlan(MYSQL_PLAN);
	this.cfClient.createService(service);
	return this.cfClient.getService(serviceName);
    }

    public boolean deleteService(String serviceName) {
	boolean deleted = false;
	if (serviceExists(serviceName)) {
	    this.cfClient.deleteService(serviceName);
	    deleted = true;
	}
	return deleted;
    }

    private boolean serviceExists(String serviceName) {
	boolean found = false;
	List<CloudService> services = this.cfClient.getServices();
	for (CloudService service : services) {
	    if (serviceName.equals(service.getName())) {
		found = true;
		break;
	    }
	}
	return found;
    }

}
