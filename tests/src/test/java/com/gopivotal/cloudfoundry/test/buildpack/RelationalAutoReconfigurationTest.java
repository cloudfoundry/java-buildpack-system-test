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
import com.gopivotal.cloudfoundry.test.support.runner.ExcludedApplications;
import com.gopivotal.cloudfoundry.test.support.service.RelationalDatabaseService;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@ExcludedApplications({"groovy", "ratpack"})
public abstract class RelationalAutoReconfigurationTest extends AbstractAutoReconfigurationTest {

    protected void assertRelationalAutoReconfiguration(Application application) {
        TestOperations testOperations = application.getTestOperations();
        Map<String, String> environmentVariables = testOperations.environmentVariables();

        assertEquals(this.servicesHolder.get(RelationalDatabaseService.class).getEndpoint(environmentVariables),
                testOperations.dataSourceUrl());
        assertEquals("ok", testOperations.dataSourceCheckAccess());
    }

}
