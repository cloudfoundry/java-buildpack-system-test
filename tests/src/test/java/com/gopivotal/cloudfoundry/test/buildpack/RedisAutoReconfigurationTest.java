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
import com.gopivotal.cloudfoundry.test.support.service.RedisService;
import com.gopivotal.cloudfoundry.test.support.util.RetryCallback;
import com.gopivotal.cloudfoundry.test.support.util.RetryTemplate;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RedisAutoReconfigurationTest extends AbstractAutoReconfigurationTest {

    private static final Long SECOND = 1000L;

    private static final Long MINUTE = 60 * SECOND;

    private static final Long INTERVAL = 5 * SECOND;

    private static final Long TIMEOUT = 5 * MINUTE;

    @CreateServices(RedisService.class)
    @Test
    @Ignore
    public void redisReconfiguration(Application application) {
        assertRedisReconfiguration(application);
    }

    private void assertRedisReconfiguration(Application application) {
        final TestOperations testOperations = application.getTestOperations();
        Map<String, String> environmentVariables = testOperations.environmentVariables();

        assertEquals(this.servicesHolder.get(RedisService.class).getEndpoint(environmentVariables),
                testOperations.redisUrl());

        RetryTemplate.retry(INTERVAL, TIMEOUT, new RetryCallback() {

            @Override
            public Boolean execute() {
                // Implemented within retry because the redis connection might not be up and running immediately
                return "ok".equals(testOperations.redisCheckAccess());
            }

            @Override
            public String getFailureMessage() {
                return String.format("Redis connection never made for '%s'", application.getName());
            }
        });
    }

}
