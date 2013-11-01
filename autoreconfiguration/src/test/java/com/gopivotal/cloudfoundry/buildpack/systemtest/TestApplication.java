// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.cloudfoundry.client.lib.domain.CloudApplication;

/**
 * {@link TestApplication} is a helper class for manipulating test applications.
 */
public class TestApplication {

    private static final String MEMORY_SYNTAX_MESSAGE_FORMAT = "Memory size from manifest.yml has bad syntax: '%s'";

    private static final String PREFIX = "java-buildpack-system-test";

    private static final String PREFIXED_SUBDOMAIN_FORMAT = "%s-%s-%s";

    private final TestClient testClient;

    private final ManifestReader manifestReader;

    private final String rawName;

    private final String prefixedName;

    private final String prefixedSubdomain;

    public TestApplication(TestClient testClient, String applicationPath) throws IOException {
	this.testClient = testClient;
	this.manifestReader = new ManifestReader(applicationPath);
	this.rawName = this.manifestReader.getName();
	this.prefixedName = prefixName(this.rawName);
	this.prefixedSubdomain = prefixSubdomain(this.rawName);
    }

    /**
     * Deploys the application and binds services to the application with the given names.
     * 
     * @param serviceNames the names of the services to be bound to the application
     * @return a {@link CloudApplication} representing the deployed application
     * @throws IOException if the application cannot be uploaded
     */
    public CloudApplication deploy(List<String> serviceNames) throws IOException {
	System.out.println(String.format("Deploying '%s' as '%s'", this.rawName, this.prefixedName));
	return this.testClient.deployApplication(this.prefixedName, this.prefixedSubdomain, this.manifestReader.getPath(),
		serviceNames, toMegabytes(this.manifestReader.getMemory()));
    }

    /**
     * Deletes the application.
     */
    public void delete() {
	this.testClient.deleteApplication(this.prefixedName);
    }

    private int toMegabytes(String memory) {
	if (memory.length() < 2) {
	    throwMemorySyntaxException(memory);
	}
	String unit = memory.substring(memory.length() - 1).toLowerCase();
	String valueString = memory.substring(0, memory.length() - 1);
	int value = -1;
	try {
	    value = Integer.parseInt(valueString);
	} catch (NumberFormatException e) {
	    throwMemorySyntaxException(memory, e);
	}
	if (value <= 0) {
	    throwMemorySyntaxException(memory);
	}
	if ("m".equals(unit)) {
	    return value;
	} else if ("g".equals(unit)) {
	    return 1024 * value;
	}
	throwMemorySyntaxException(memory);
	return -1; // never executed
    }

    private void throwMemorySyntaxException(String memory) {
	throw new IllegalArgumentException(String.format(MEMORY_SYNTAX_MESSAGE_FORMAT, memory));
    }

    private void throwMemorySyntaxException(String memory, Exception e) {
	throw new IllegalArgumentException(String.format(MEMORY_SYNTAX_MESSAGE_FORMAT, memory), e);
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
