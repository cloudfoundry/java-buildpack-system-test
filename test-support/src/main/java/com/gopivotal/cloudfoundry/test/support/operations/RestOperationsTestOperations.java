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

import com.gopivotal.cloudfoundry.test.support.util.JdbcUrlNormalizer;
import com.gopivotal.cloudfoundry.test.support.util.RetryCallback;
import com.gopivotal.cloudfoundry.test.support.util.RetryTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.util.List;
import java.util.Map;

final class RestOperationsTestOperations extends AbstractTestOperations {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Long connectionInterval;

    private final Long connectionTimeout;

    private final String host;

    private final RestOperations restOperations;

    RestOperationsTestOperations(Long connectionInterval, Long connectionTimeout, String host,
                                 RestOperations restOperations) {
        this.connectionInterval = connectionInterval;
        this.connectionTimeout = connectionTimeout;
        this.host = host;
        this.restOperations = restOperations;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> classPath() {
        this.logger.debug("Getting class path");
        return (List<String>) this.restOperations.getForObject("http://{host}/class-path", List.class, this.host);
    }

    @Override
    public String dataSourceCheckAccess() {
        return this.restOperations.getForObject("http://{host}/datasource/check-access", String.class, this.host);
    }

    @Override
    public URI dataSourceUrl() {
        String urlString = this.restOperations.getForObject("http://{host}/datasource/url", String.class, this.host);
        return JdbcUrlNormalizer.normalize(urlString);
    }

    @Override
    public String redisCheckAccess() {
        return this.restOperations.getForObject("http://{host}/redis/check-access", String.class, this.host);
    }

    @Override
    public URI redisUrl() {
        return URI.create(this.restOperations.getForObject("http://{host}/redis/url", String.class, this.host));
    }

    @Override
    public String mongoDbCheckAccess() {
        return this.restOperations.getForObject("http://{host}/mongodb/check-access", String.class, this.host);
    }

    @Override
    public URI mongoDbUrl() {
        return URI.create(this.restOperations.getForObject("http://{host}/mongodb/url", String.class, this.host));
    }

    @Override
    public String rabbitCheckAccess() {
        return this.restOperations.getForObject("http://{host}/rabbit/check-access", String.class, this.host);
    }

    @Override
    public URI rabbitUrl() {
        return URI.create(this.restOperations.getForObject("http://{host}/rabbit/url", String.class, this.host));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> environmentVariables() {
        this.logger.debug("Getting environment variables");
        return (Map<String, String>) this.restOperations.getForObject("http://{host}/environment-variables",
                Map.class, this.host);
    }

    @Override
    public String health() {
        this.logger.debug("Getting health");
        return this.restOperations.getForObject("http://{host}/", String.class, this.host);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> inputArguments() {
        this.logger.debug("Getting input arguments");
        return (List<String>) this.restOperations.getForObject("http://{host}/input-arguments", List.class, this.host);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> systemProperties() {
        this.logger.debug("Getting system properties");
        return (Map<Object, Object>) this.restOperations.getForObject("http://{host}/system-properties",
                Map.class, this.host);
    }

    @Override
    public void waitForStart(String name) {
        RetryTemplate.retry(this.connectionInterval, this.connectionTimeout, new RetryCallback() {

            private Logger logger = LoggerFactory.getLogger(this.getClass());

            @Override
            public Boolean execute() {
                this.logger.debug("Checking for connection to http://{}/", RestOperationsTestOperations.this.host);
                HttpStatus statusCode = RestOperationsTestOperations.this.restOperations.getForEntity
                        ("http://{host}/", String.class, RestOperationsTestOperations.this.host).getStatusCode();
                this.logger.debug("Received {} from http://{}/", statusCode, RestOperationsTestOperations.this.host);

                return HttpStatus.OK == statusCode;
            }

            @Override
            public String getFailureMessage() {
                return String.format("Application '%s' did not start quickly enough", name);
            }

        });
    }
}
