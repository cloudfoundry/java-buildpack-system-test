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

package com.gopivotal.cloudfoundry.test.support.util;

import java.io.File;

/**
 * TODO Document Manifest
 */
public interface Manifest {

    /**
     * Gets the application name.
     *
     * @return the application name
     */
    String getName();

    /**
     * Gets the memory size.
     *
     * @return the memory size in megabytes
     */
    int getMemory();

    /**
     * Gets the number of instances.
     *
     * @return the number of instances
     */
    int getInstances();

    /**
     * Gets the path of the application on the file system.
     *
     * @return a {@link File} the path of the application
     */
    File getPath();

    /**
     * Gets the buildpack for the application.
     *
     * @return the string URL of the buildpack
     */
    String getBuildpack();

}
