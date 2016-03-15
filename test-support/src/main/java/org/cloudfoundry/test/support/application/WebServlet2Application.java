/*
 * Copyright 2013-2016 the original author or authors.
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

package org.cloudfoundry.test.support.application;

import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.test.support.NameFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.AsyncRestOperations;

import java.io.File;

@Component
public final class WebServlet2Application extends AbstractApplication {

    @Autowired
    WebServlet2Application(String buildpack,
                           CloudFoundryOperations cloudFoundryOperations,
                           @Value("${applications.webServlet2.location}") File location,
                           NameFactory nameFactory,
                           @Value("${applications.webServlet2.prefix}") String prefix,
                           AsyncRestOperations restOperations) {
        super(buildpack, cloudFoundryOperations, location, nameFactory.getName(prefix), restOperations);
    }

}
