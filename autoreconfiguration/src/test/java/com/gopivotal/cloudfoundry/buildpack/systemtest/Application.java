// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

/**
 * TODO Document Application
 */
public interface Application {

    Application push();

    Application bind(Service... services);

    Application start();

    Application stop();

    Application unbind(Service... services);

    void delete();

}
