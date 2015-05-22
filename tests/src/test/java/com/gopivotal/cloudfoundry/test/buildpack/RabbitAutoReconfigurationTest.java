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
import com.gopivotal.cloudfoundry.test.support.service.CreateServices;
import com.gopivotal.cloudfoundry.test.support.service.RabbitService;
import com.gopivotal.cloudfoundry.test.support.util.RetryCallback;
import com.gopivotal.cloudfoundry.test.support.util.RetryTemplate;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RabbitAutoReconfigurationTest extends AbstractAutoReconfigurationTest {

    private static final Long SECOND = 1000L;

    private static final Long MINUTE = 60 * SECOND;

    private static final Long INTERVAL = 5 * SECOND;

    private static final Long TIMEOUT = 5 * MINUTE;

    @CreateServices(RabbitService.class)
    @Test
    public void rabbitReconfiguration(Application application) {
        assertRabbitAutoReconfiguration(application);
    }

    private void assertRabbitAutoReconfiguration(Application application) {
        final TestOperations testOperations = application.getTestOperations();
        Map<String, String> environmentVariables = testOperations.environmentVariables();

        assertEquals(this.servicesHolder.get(RabbitService.class).getEndpoint(environmentVariables),
                testOperations.rabbitUrl());

        RetryTemplate.retry(INTERVAL, TIMEOUT, new RetryCallback() {

            @Override
            public Boolean execute() {
                // Implemented within retry because the rabbit connection might not be up and running immediately
                return "ok".equals(testOperations.rabbitCheckAccess());
            }

            @Override
            public String getFailureMessage() {
                return String.format("Rabbit connection never made for '%s'", application.getName());
            }
        });

    }
}
