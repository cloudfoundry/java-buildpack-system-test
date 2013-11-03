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

final class StandardMemorySizeParser implements MemorySizeParser {

    private static final String MEMORY_SYNTAX_MESSAGE_FORMAT = "Memory size from has bad syntax: '%s'";

    private static final String CHAR_GIBI = "g";

    private static final String CHAR_MIBI = "m";

    private static final Integer GIBI = 1024;

    @Override
    public Integer toMibiBytes(String raw) {
        ensureLength(raw);

        Integer value = getValue(raw);
        ensurePositive(value, raw);

        String unit = getUnit(raw);
        if (CHAR_GIBI.equals(unit)) {
            return GIBI * value;
        } else if (CHAR_MIBI.equals(unit)) {
            return value;
        }

        throw createException(raw);
    }

    private IllegalArgumentException createException(String raw) {
        return new IllegalArgumentException(String.format(MEMORY_SYNTAX_MESSAGE_FORMAT, raw));
    }

    private IllegalArgumentException createException(String raw, Exception e) {
        return new IllegalArgumentException(String.format(MEMORY_SYNTAX_MESSAGE_FORMAT, raw), e);
    }

    private void ensureLength(String raw) {
        if (raw.length() < 2) {
            throw createException(raw);
        }
    }

    private void ensurePositive(Integer value, String raw) {
        if (value <= 0) {
            throw createException(raw);
        }
    }

    private String getUnit(String raw) {
        return raw.substring(raw.length() - 1).toLowerCase();
    }

    private Integer getValue(String raw) {
        try {
            return Integer.parseInt(raw.substring(0, raw.length() - 1));
        } catch (NumberFormatException e) {
            throw createException(raw, e);
        }
    }

}
