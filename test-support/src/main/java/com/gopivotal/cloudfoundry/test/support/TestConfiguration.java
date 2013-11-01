// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.test.support;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.MalformedURLException;
import java.net.URI;

/**
 * TODO Document TestConfiguration
 */
@Configuration
public class TestConfiguration {

    // TODO: Make sure that missing environment variables are diagnosed

    @Value("#{systemEnvironment['CF_ORG']}")
    private volatile String orgName;

    @Value("#{systemEnvironment['CF_PASSWORD']}")
    private volatile String password;

    @Value("#{systemEnvironment['CF_SPACE']}")
    private volatile String spaceName;

    @Value("#{systemEnvironment['CF_TARGET']}")
    private volatile String target;

    @Value("#{systemEnvironment['CF_USERNAME']}")
    private volatile String user;

    @Bean(destroyMethod = "logout")
    CloudFoundryOperations cloudFoundryOperations() throws MalformedURLException {
        CloudCredentials credentials = new CloudCredentials(this.user, this.password);
        CloudFoundryOperations cloudFoundryOperations = new CloudFoundryClient(credentials, URI.create(this.target)
                .toURL(), this.orgName, this.spaceName);
        cloudFoundryOperations.login();
        return cloudFoundryOperations;
    }

}
