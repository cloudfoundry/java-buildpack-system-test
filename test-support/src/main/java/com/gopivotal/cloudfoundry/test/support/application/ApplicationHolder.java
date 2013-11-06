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

/**
 * A holder for the {@link Application} associated with a test
 */
public interface ApplicationHolder {

    /**
     * Clears the application held by this holder
     */
    void clear();

    /**
     * Returns the application held by this holder, or {@code null} if none currently exists
     *
     * @return the application held by this holder, or {@code null} if none currently exists
     */
    Application get();

    /**
     * Sets the application held by this holder
     *
     * @param application the application to be held by this holder
     */
    void set(Application application);

}
