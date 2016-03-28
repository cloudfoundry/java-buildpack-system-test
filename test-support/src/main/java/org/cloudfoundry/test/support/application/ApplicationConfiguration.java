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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Configuration
class ApplicationConfiguration {

    @Autowired
    private List<Application> applications;

    @PostConstruct
    void push() {
        Flux
            .fromIterable(this.applications)
            .flatMap(Application::push)
            .doOnSubscribe(s -> registerShutdownHook())
            .after()
            .get(Duration.ofMinutes(15));
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Flux
                .fromIterable(this.applications)
                .flatMap(Application::delete)
                .after()
                .get(Duration.ofMinutes(15));
        }));
    }

}
