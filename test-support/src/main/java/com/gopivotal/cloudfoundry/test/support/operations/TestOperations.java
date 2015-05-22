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

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * An abstraction for handling operations
 */
public interface TestOperations {

    /**
     * Returns the application's classpath
     *
     * @return the application's classpath
     */
    List<String> classPath();

    /**
     * Returns whether the DataSource can access the database
     *
     * @return whether the DataSource can access the database
     */
    String dataSourceCheckAccess();

    /**
     * Returns the URL that the DataSource is connected to.  The URL is normalized to account for differences in
     * underlying implementations.
     *
     * @return the URL that the DataSource is connected to
     * @see com.gopivotal.cloudfoundry.test.support.util.JdbcUrlNormalizer
     */
    URI dataSourceUrl();

    /**
     * Returns the application's environment variables
     *
     * @return the application's environment variables
     */
    Map<String, String> environmentVariables();

    /**
     * Returns the application's health
     *
     * @return the application's health
     */
    String health();

    /**
     * Returns the application's input arguments
     *
     * @return the application's input arguments
     */
    List<String> inputArguments();

    /**
     * Whether a JAR is on the application's classpath
     *
     * @param name the name of the jar to check for, without the {@code .jar} extension
     *
     * @return {@code true} if the JAR is on the classpath, {@code false} otherwise
     */
    Boolean isJarOnClassPath(String name);

    /**
     * Returns the application's system properties
     *
     * @return the application's system properties
     */
    Map<Object, Object> systemProperties();

    /**
     * Blocks until the test application has started successfully
     *
     * @param name the name of the application to block on
     *
     */
    void waitForStart(String name);

    /**
     * Returns whether the service connector can access the redis service
     */
    String redisCheckAccess();

    /**
     * Returns the URL that the Redis service is connected to
     */
    URI redisUrl();

    /**
     * Returns whether the service connector can access the mongodb service
     */
    String mongoDbCheckAccess();

    /**
     * Returns the URL that the mongodb service is connected to
     */
    URI mongoDbUrl();

    /**
     * Returns whether the service connector can access the rabbit service
     */
    String rabbitCheckAccess();

    /**
     * Returns the URL that the rabbit service is connected to
     */
    URI rabbitUrl();

}
