// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.test.support;

import com.gopivotal.cloudfoundry.test.support.util.Manifest;
import com.gopivotal.cloudfoundry.test.support.util.YamlManifest;
import org.apache.commons.lang3.RandomStringUtils;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * TODO Document XApplication
 */
public class CloudFoundryApplication implements Application {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    public CloudFoundryApplication(CloudFoundryOperations cfOperations, String testApplicationName) {
        this.cfOperations = cfOperations;
        String testApplicationPath = "../vendor/java-test-applications/" + testApplicationName;
        this.manifest = new YamlManifest(testApplicationPath);
        String rawName = this.manifest.getName();
        this.prefixedName = prefixName(rawName);
        String prefixedSubdomain = prefixSubdomain(rawName);


        this.logger.info("Creating application '{}'", this.prefixedName);

        Staging staging = new Staging(null, JAVA_BUILDPACK_URL);
        this.defaultDomainName = getDefaultDomain(cfOperations.getDomainsForOrg()).getName();

        List<String> uris = Arrays.asList(computeAppUri(prefixedSubdomain));
        cfOperations.createApplication(this.prefixedName, staging, this.manifest.getMemory(), uris,
                Arrays.<String>asList());
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
            String serviceName = service.getName();

            this.logger.info("Binding service '{}' to '{}'", serviceName, this.prefixedName);
            this.cfOperations.bindService(this.prefixedName, serviceName);
        }
        return this;
    }

    @Override
    public Application start() {
        this.logger.info("Start application '{}'", this.prefixedName);
        this.cfOperations.startApplication(this.prefixedName);
        return this;
    }

    @Override
    public Application stop() {
        this.logger.info("Stopping application '{}'", this.prefixedName);
        this.cfOperations.stopApplication(this.prefixedName);
        return this;
    }

    @Override
    public Application unbind(Service... services) {
        for (Service service : services) {
            String serviceName = service.getName();

            this.logger.info("Unbinding service '{}' from '{}'", serviceName, this.prefixedName);
            this.cfOperations.unbindService(this.prefixedName, serviceName);
        }
        return this;
    }

    @Override
    public void delete() {
        if (applicationExists(this.prefixedName)) {
            this.logger.info("Deleting application '{}'", this.prefixedName);
            this.cfOperations.deleteApplication(this.prefixedName);
        }
    }

    private boolean applicationExists(String appName) {
        boolean found = false;
        List<CloudApplication> apps = this.cfOperations.getApplications();
        app_loop:
        for (CloudApplication app : apps) {
            if (appName.equals(app.getName())) {
                found = true;
                break app_loop;
            }
        }
        return found;
    }

}
