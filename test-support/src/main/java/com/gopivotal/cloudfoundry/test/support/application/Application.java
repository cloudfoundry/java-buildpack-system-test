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

package com.gopivotal.cloudfoundry.test.support.application;

import com.gopivotal.cloudfoundry.test.support.operations.TestOperations;
import com.gopivotal.cloudfoundry.test.support.service.Service;
import com.gopivotal.cloudfoundry.test.support.util.Deletable;

/**
 * Represents an application in Cloud Foundry
 */
public interface Application extends Deletable {

    /**
     * Pushes the contents of an application
     *
     * @return this {@link Application}
     */
    Application push();

    /**
     * Bind services to an application
     *
     * @param services the services to bind to the application
     *
     * @return this {@link Application}
     */
    Application bind(Service... services);

    /**
     * Returns a {@link TestOperations} that communicates with the application's endpoint
     *
     * @return a {@link TestOperations} that communicates with the application's endpoint
     */
    TestOperations getTestOperations();

    /**
     * Start an application
     *
     * @return this {@link Application}
     */
    Application start();

    /**
     * Stop an application
     *
     * @return this {@link Application}
     */
    Application stop();

    /**
     * Unbind services from an application
     *
     * @param services the services to unbind from the application
     *
     * @return this {@link Application}
     */
    Application unbind(Service... services);

    /**
     * Get the name of the application
     *
     * @return String name
     */
    String getName();

}
