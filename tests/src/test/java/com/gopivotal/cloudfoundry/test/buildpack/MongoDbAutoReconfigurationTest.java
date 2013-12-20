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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import com.gopivotal.cloudfoundry.test.support.application.Application;
import com.gopivotal.cloudfoundry.test.support.operations.TestOperations;
import com.gopivotal.cloudfoundry.test.support.runner.ExcludedApplications;
import com.gopivotal.cloudfoundry.test.support.service.CreateServices;
import com.gopivotal.cloudfoundry.test.support.service.MongoDbService;

@ExcludedApplications({"grails", "groovy", "java-main", "java-main-with-web-inf", "spring-boot-cli", "web", "play"})
public class MongoDbAutoReconfigurationTest extends AbstractAutoReconfigurationTest {
    @CreateServices(MongoDbService.class)
    @Test
    public void mongoDbReconfiguration(Application application) {
        assertMongoDbAutoReconfiguration(application);
    }
    
    private void assertMongoDbAutoReconfiguration(Application application) {
        TestOperations testOperations = application.getTestOperations();
        Map<String, String> environmentVariables = testOperations.environmentVariables();

        assertEquals(this.servicesHolder.get(MongoDbService.class).getEndpoint(environmentVariables),
                testOperations.mongoDbUrl());
        assertEquals("ok", testOperations.mongoDbCheckAccess());
    }
}
