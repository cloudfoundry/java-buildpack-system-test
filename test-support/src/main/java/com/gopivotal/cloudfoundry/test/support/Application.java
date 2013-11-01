// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.test.support;

import com.gopivotal.cloudfoundry.test.support.util.Deletable;

/**
 * TODO Document Application
 */
public interface Application extends Deletable{

    Application push();

    Application bind(Service... services);

    Application start();

    Application stop();

    Application unbind(Service... services);


}
