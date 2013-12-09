package com.gopivotal.cloudfoundry.test.support.service;

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import com.gopivotal.cloudfoundry.test.support.util.JdbcUrlNormalizer;
import org.cloudfoundry.client.lib.CloudFoundryOperations;

import java.net.URI;
import java.util.Map;

/**
 * A base class for all services that are relational databases
 */
public abstract class RelationalDatabaseService extends AbstractService {

    public RelationalDatabaseService(CloudFoundryOperations cloudFoundryOperations, String label, String plan,
                                     RandomizedNameFactory randomizedNameFactory) {
        super(cloudFoundryOperations, label, plan, randomizedNameFactory);
    }

    @Override
    public final URI getEndpoint(Map<String, String> environmentVariables) {
        Map<String, ?> credentials = getCredentials(environmentVariables);
        return JdbcUrlNormalizer.normalize((String) credentials.get("uri"));
    }

}
