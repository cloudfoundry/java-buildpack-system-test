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

package com.gopivotal.cloudfoundry.test.buildpack;

import com.gopivotal.cloudfoundry.test.support.application.CreateApplication;
import com.gopivotal.cloudfoundry.test.support.service.CreateServices;
import com.gopivotal.cloudfoundry.test.support.service.ClearDbService;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public final class AutoReconfigurationTest extends AbstractTest {

    @Test
    @CreateServices(ClearDbService.class)
    @CreateApplication("web-servlet-2-application")
    public void autoReconfigurationServlet2() throws IOException {
        assertEquals("ClearDBService concrete type TBD once autoreconfiguration works",
                getTestOperations().datasourceClassName());
    }

    @Test
    @CreateServices(ClearDbService.class)
    @CreateApplication("web-application")
    public void autoReconfigurationServlet3() throws IOException {
        assertEquals("ClearDBService concrete type TBD once autoreconfiguration works for servlet 3",
                getTestOperations().datasourceClassName());
    }


}
