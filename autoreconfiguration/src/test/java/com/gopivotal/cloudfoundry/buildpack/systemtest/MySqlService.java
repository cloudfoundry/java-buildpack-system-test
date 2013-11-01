// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;

/**
 * TODO Document MySqlService
 */
public class MySqlService implements Service {

    private static final String MYSQL_SERVICE_NAME = "java-buildpack-system-test-mysql";

    private static final String MYSQL_PLAN = "spark";

    private static final String MY_SQL_LABEL = "cleardb";

    private final CloudService mySqlService;

    private final CloudFoundryOperations cloudFoundryOperations;

    /**
     * @param cloudFoundryOperations
     */
    public MySqlService(CloudFoundryOperations cloudFoundryOperations) {
	this.cloudFoundryOperations = cloudFoundryOperations;
	System.out.println(String.format("Creating MySQL service named '%s'", MYSQL_SERVICE_NAME));
	List<CloudServiceOffering> serviceOfferings = cloudFoundryOperations.getServiceOfferings();
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
	CloudService service = new CloudService(CloudEntity.Meta.defaultMeta(), MYSQL_SERVICE_NAME);
	service.setProvider("core");
	service.setLabel(MY_SQL_LABEL);
	service.setVersion(databaseServiceOffering.getVersion());
	service.setPlan(MYSQL_PLAN);
	cloudFoundryOperations.createService(service);
	this.mySqlService = cloudFoundryOperations.getService(MYSQL_SERVICE_NAME);
    }

    @Override
    public void delete() {
	this.cloudFoundryOperations.deleteService(this.mySqlService.getName());
    }

    @Override
    public String getName() {
	return this.mySqlService.getName();
    }

}
