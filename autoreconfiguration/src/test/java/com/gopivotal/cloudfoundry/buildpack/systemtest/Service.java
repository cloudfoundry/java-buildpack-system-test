// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

/**
 * TODO Document Service
 */
public interface Service {

    void delete();

    /**
     * @return
     */
    String getName();

}
