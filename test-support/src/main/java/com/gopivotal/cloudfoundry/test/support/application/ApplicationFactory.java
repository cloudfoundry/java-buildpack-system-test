package com.gopivotal.cloudfoundry.test.support.application;

/**
 * A factory for creating an {@link Application} instance
 */
public interface ApplicationFactory {

    /**
     * Create a new instance
     *
     * @return a new instance
     */
    Application create(String name);

}
