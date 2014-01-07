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

package com.gopivotal.cloudfoundry.test.support.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@Component
final class StandardRandomizedNameFactory implements RandomizedNameFactory {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String nameFormat;

    private final Pattern namePattern;

    @Autowired
    StandardRandomizedNameFactory(@Value("${cf.discriminator:${user.name}}") String discriminator) {
        this.nameFormat = String.format("system-test-%s-%%s-%%06d", discriminator);
        this.namePattern = Pattern.compile(String.format("system-test-%s-.*-[\\d]{6}", discriminator));
    }

    @Override
    public String create(String stem) {
        return String.format(this.nameFormat, stem, RANDOM.nextInt(1000000));
    }

    @Override
    public Boolean matches(String name) {
        return this.namePattern.matcher(name).matches();
    }
}
