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

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public final class AbstractServicesHolderTest {

    private final Service service = mock(Service.class);

    private final AbstractServicesHolder servicesHolder = new StubServicesHolder();

    @Test
    public void test() {
        assertEquals(0, this.servicesHolder.get().length);
        this.servicesHolder.add(this.service);
        assertSame(this.service, this.servicesHolder.get()[0]);
        this.servicesHolder.clear();
        assertEquals(0, this.servicesHolder.get().length);
    }

    private static class StubServicesHolder extends AbstractServicesHolder {

    }

}
