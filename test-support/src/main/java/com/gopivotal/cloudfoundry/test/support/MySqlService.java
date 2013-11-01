/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gopivotal.cloudfoundry.test.support;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * TODO Document MySqlService
 */
public class MySqlService implements Service {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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

        logger.info("Creating MySQL service named '{}'", MYSQL_SERVICE_NAME);

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
        this.logger.info("Deleting MySQL service named '{}'", getName());
        this.cloudFoundryOperations.deleteService(getName());
    }

    @Override
    public String getName() {
        return this.mySqlService.getName();
    }

}
