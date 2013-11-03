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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link TestOperations} that uses a {@link RestOperations}.
 */
public final class RestOperationsTestOperations implements TestOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String host;

    private final RestOperations restOperations;

    /**
     * Creates a new instance
     *
     * @param host the host of the application's REST endpoints
     */
    public RestOperationsTestOperations(String host) {
        this(host, createRestTemplate());
    }

    RestOperationsTestOperations(String host, RestOperations restOperations) {
        this.host = host;
        this.restOperations = restOperations;
    }

    private static RestOperations createRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

            @Override
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }
        });

        return restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> classPath() {
        this.logger.debug("Getting class path");
        return (List<String>) this.restOperations.getForObject("http://{host}/class-path", List.class, this.host);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> environmentVariables() {
        this.logger.debug("Getting environment variables");
        return (Map<String, String>) this.restOperations.getForObject("http://{host}/environment-variables",
                Map.class, this.host);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> inputArguments() {
        this.logger.debug("Getting input arguments");
        return (List<String>) this.restOperations.getForObject("http://{host}/input-arguments", List.class, this.host);
    }

    @Override
    public Boolean isConnected() {
        this.logger.debug("Checking for connection");
        return HttpStatus.OK == this.restOperations.getForEntity("http://{host}/class-path", String.class,
                this.host).getStatusCode();
    }

    @Override
    public Boolean isJarOnClassPath(String name) {
        String path = String.format("%s.jar", name);

        for (String entry : classPath()) {
            if (entry.endsWith(path)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> systemProperties() {
        this.logger.debug("Getting system properties");
        return (Map<Object, Object>) this.restOperations.getForObject("http://{host}/system-properties",
                Map.class, this.host);
    }
}
