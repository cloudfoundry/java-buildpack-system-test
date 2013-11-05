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

package com.gopivotal.cloudfoundry.test.support.operations;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class RestOperationsTestOperationsTest {

    private final RestOperations restOperations = mock(RestOperations.class);

    private final RestOperationsTestOperations testOperations = new RestOperationsTestOperations(0L, 10L, "test-host",
            this.restOperations);

    @Test
    public void classPath() throws Exception {
        List<String> classPath = Arrays.asList();
        when(this.restOperations.getForObject("http://{host}/class-path", List.class,
                "test-host")).thenReturn(classPath);

        List<String> value = this.testOperations.classPath();

        assertSame(classPath, value);
    }

    @Test
    public void environmentVariables() throws Exception {
        Map<String, String> environmentVariables = new HashMap<>();
        when(this.restOperations.getForObject("http://{host}/environment-variables", Map.class,
                "test-host")).thenReturn(environmentVariables);

        Map<String, String> value = this.testOperations.environmentVariables();

        assertSame(environmentVariables, value);
    }

    @Test
    public void inputArguments() throws Exception {
        List<String> inputArguments = Arrays.asList();
        when(this.restOperations.getForObject("http://{host}/input-arguments", List.class,
                "test-host")).thenReturn(inputArguments);

        List<String> value = this.testOperations.inputArguments();

        assertSame(inputArguments, value);
    }

    @Test
    public void systemProperties() throws Exception {
        Map<Object, Object> systemProperties = new HashMap<>();
        when(this.restOperations.getForObject("http://{host}/system-properties", Map.class,
                "test-host")).thenReturn(systemProperties);

        Map<Object, Object> value = this.testOperations.systemProperties();

        assertSame(systemProperties, value);
    }

    @Test
    public void waitForStart() throws Exception {
        when(this.restOperations.getForEntity("http://{host}/class-path", String.class,
                "test-host")).thenReturn(new ResponseEntity<String>(HttpStatus.OK));

        this.testOperations.waitForStart();
    }

    @Test(expected = IllegalStateException.class)
    public void waitForStartTimeout() throws Exception {
        when(this.restOperations.getForEntity("http://{host}/class-path", String.class,
                "test-host")).thenReturn(new ResponseEntity<String>(HttpStatus.BAD_REQUEST));

        this.testOperations.waitForStart();
    }

}
