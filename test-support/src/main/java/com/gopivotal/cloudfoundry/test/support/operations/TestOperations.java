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
     * Returns the application's environment variables
     *
     * @return the application's environment variables
     */
    Map<String, String> environmentVariables();

    /**
     * Returns the application's input arguments
     *
     * @return the application's input arguments
     */
    List<String> inputArguments();

    /**
     * Returns whether the application can be connected to
     *
     * @return whether the application can be connected to
     */
    Boolean isConnected();

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

}
