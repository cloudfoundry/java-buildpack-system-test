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

package com.gopivotal.cloudfoundry.test.buildpack;

import com.gopivotal.cloudfoundry.test.support.application.Application;
import com.gopivotal.cloudfoundry.test.support.operations.TestOperations;
import com.gopivotal.cloudfoundry.test.support.service.ClearDbService;
import com.gopivotal.cloudfoundry.test.support.service.CreateServices;
import com.gopivotal.cloudfoundry.test.support.service.ElephantSqlService;
import com.gopivotal.cloudfoundry.test.support.service.RelationalDatabaseService;
import com.gopivotal.cloudfoundry.test.support.service.ServicesHolder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public final class AutoReconfigurationTest extends AbstractTest {

    @Autowired
    private volatile ServicesHolder servicesHolder;

    @CreateServices(ClearDbService.class)
    @Test
    public void mysqlReconfiguration(Application application) {
        relationalAutoReconfiguration(application);
    }

    @CreateServices(ElephantSqlService.class)
    @Test
    public void postgresReconfiguration(Application application) {
        relationalAutoReconfiguration(application);
    }

    private void relationalAutoReconfiguration(Application application) {

        TestOperations testOperations = application.getTestOperations();
        Map<String, String> environmentVariables = testOperations.environmentVariables();

        assertEquals(this.servicesHolder.get(RelationalDatabaseService.class).getEndpoint(environmentVariables),
                testOperations.dataSourceUrl());
        assertEquals("ok", testOperations.dataSourceCheckAccess());
    }

}
