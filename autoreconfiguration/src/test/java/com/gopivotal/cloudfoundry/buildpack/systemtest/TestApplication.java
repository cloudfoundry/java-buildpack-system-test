// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;

/**
 * TODO Document XApplication
 */
public class TestApplication implements Application {

    private static final String JAVA_BUILDPACK_URL = "https://github.com/cloudfoundry/java-buildpack.git"; // TODO:
													   // convert to
													   // env var

    private static final String PREFIX = "java-buildpack-system-test";

    private static final String PREFIXED_SUBDOMAIN_FORMAT = "%s-%s-%s";

    private final Manifest manifest;

    private final CloudFoundryOperations cfOperations;

    private final String prefixedName;

    private final String defaultDomainName;

    /**
     * @param cfOperations
     * @param string
     */
    public TestApplication(CloudFoundryOperations cfOperations, String testApplicationName) {
	this.cfOperations = cfOperations;
	String testApplicationPath = "../vendor/java-test-applications/" + testApplicationName;
	this.manifest = new ManifestReader(testApplicationPath);
	String rawName = this.manifest.getName();
	this.prefixedName = prefixName(rawName);
	String prefixedSubdomain = prefixSubdomain(rawName);
	System.out.println(String.format("Creating '%s' as '%s'", rawName, this.prefixedName));

	Staging staging = new Staging(null, JAVA_BUILDPACK_URL);
	this.defaultDomainName = getDefaultDomain(cfOperations.getDomainsForOrg()).getName();

	List<String> uris = Arrays.asList(computeAppUri(prefixedSubdomain));
	cfOperations.createApplication(this.prefixedName, staging, this.manifest.getMemory(), uris,
		Arrays.<String> asList());
    }

    private String computeAppUri(String subdomain) {
	return subdomain + "." + this.defaultDomainName;
    }

    private CloudDomain getDefaultDomain(List<CloudDomain> domains) {
	for (CloudDomain domain : domains) {
	    if (domain.getOwner().getName().equals("none")) {
		return domain;
	    }
	}
	return null;
    }

    @Override
    public Application push() {
	try {
	    this.cfOperations.uploadApplication(this.prefixedName, this.manifest.getPath());
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	return this;
    }

    @Override
    public Application bind(Service... services) {
	for (Service service : services) {
	    this.cfOperations.bindService(this.prefixedName, service.getName());
	}
	return this;
    }

    @Override
    public Application start() {
	this.cfOperations.startApplication(this.prefixedName);
	return this;
    }

    @Override
    public Application stop() {
	this.cfOperations.stopApplication(this.prefixedName);
	return this;
    }

    @Override
    public Application unbind(Service... services) {
	for (Service service : services) {
	    this.cfOperations.unbindService(this.prefixedName, service.getName());
	}
	return this;
    }

    @Override
    public void delete() {
	if (applicationExists(this.prefixedName)) {
	    this.cfOperations.deleteApplication(this.prefixedName);
	}
    }

    private boolean applicationExists(String appName) {
	boolean found = false;
	List<CloudApplication> apps = this.cfOperations.getApplications();
	app_loop: for (CloudApplication app : apps) {
	    if (appName.equals(app.getName())) {
		found = true;
		break app_loop;
	    }
	}
	return found;
    }

    private static String prefixName(String rawName) {
	return PREFIX + "-" + rawName;
    }

    private static String prefixSubdomain(String subdomain) {
	return String.format(PREFIXED_SUBDOMAIN_FORMAT, PREFIX, randomSubdomainPortion(), subdomain);
    }

    private static String randomSubdomainPortion() {
	return RandomStringUtils.randomAlphanumeric(6).toLowerCase();
    }

}