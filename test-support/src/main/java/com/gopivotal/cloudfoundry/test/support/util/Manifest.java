// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
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
