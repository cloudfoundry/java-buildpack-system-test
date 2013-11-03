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

import com.gopivotal.cloudfoundry.test.support.TestConfiguration;
import com.gopivotal.cloudfoundry.test.support.rules.Applications;
import com.gopivotal.cloudfoundry.test.support.rules.ApplicationsRule;
import com.gopivotal.cloudfoundry.test.support.rules.Services;
import com.gopivotal.cloudfoundry.test.support.service.ClearDbService;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.initializer.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public final class AutoReconfigurationTest {

    @Rule
    @Autowired
    public volatile RuleChain ruleChain;

    @Autowired
    public volatile ApplicationsRule applicationsRule;

    @Test
    @Services(ClearDbService.class)
    @Applications("web-application")
    public void autoReconfiguration() throws IOException {
        System.out.println(this.applicationsRule.getApplication().getTestOperations().classPath());
    }

}
