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

package com.gopivotal.cloudfoundry.test.support.service;

/**
 * A holder for the {@link Service}s associate with a test
 */
public interface ServicesHolder {

    /**
     * Adds to the services held by this holder
     *
     * @param services the services to add to the services held by this holder
     */
    void add(Service... services);

    /**
     * Clears the services held by this holder
     */
    void clear();

    /**
     * Returns the services held by this holder
     *
     * @return the services held by this holder
     */
    Service[] get();

    /**
     * Returns a service of a particular type from the services held by this holder
     *
     * @param klass the type of the service to return
     * @param <T>   the type of the service to return
     *
     * @return a service of a particular type from the services held by this holder
     */
    <T extends Service> T get(Class<T> klass);

}
