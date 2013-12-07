package com.gopivotal.cloudfoundry.test.support.service;

import java.net.URI;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudFoundryOperations;

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;

public abstract class RelationalDbService extends AbstractService {

    public RelationalDbService(CloudFoundryOperations cloudFoundryOperations, String label, String plan,
            RandomizedNameFactory randomizedNameFactory) {
        super(cloudFoundryOperations, label, plan, randomizedNameFactory);
    }

    @Override
    public URI getEndpoint(Map<String, String> environmentVariables) {
        Map<String, Object> credentials = getCredentials(environmentVariables);
        // Use "uri" and not "jdbcUrl", since the latter isn't available for postgresql
        return URI.create(getTestableUrl((String) credentials.get("uri")));
    }

}