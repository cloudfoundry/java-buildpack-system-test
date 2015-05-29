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

package com.gopivotal.cloudfoundry.test.support.runner;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class ApplicationSpecificFrameworkMethodTest {

    private final ApplicationSpecificFrameworkMethod method;

    public ApplicationSpecificFrameworkMethodTest() throws NoSuchMethodException {
        this.method = new ApplicationSpecificFrameworkMethod
                ("test-application", Object.class.getMethod("hashCode"));
    }

    @Test
    public void getApplication() throws Exception {
        assertEquals("test-application", this.method.getApplication());
    }

    @Test
    public void getName() throws Exception {
        assertEquals("hashCode", this.method.getName());
    }

}
