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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
final class DefaultApplicationDirectory implements ApplicationDirectory {

    private final Map<Class, Application> applications;

    @Autowired
    DefaultApplicationDirectory(List<? extends Application> applications) {
        this.applications = applications.stream()
            .collect(Collectors.toMap(Application::getClass, a -> a));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Application> T get(Class<T> type) {
        return (T) this.applications.get(type);
    }

    @PostConstruct
    void push() {
        Flux
            .fromIterable(this.applications.values())
            .flatMap(Application::push)
            .doOnComplete(this::registerShutdownHook)
            .after()
            .get(Duration.ofMinutes(15));
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Flux
                .fromIterable(this.applications.values())
                .flatMap(Application::delete)
                .after()
                .get(Duration.ofMinutes(15));
        }));
    }

}
