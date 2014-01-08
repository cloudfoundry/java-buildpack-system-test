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

/**
 * An template for automating retry behavior
 */
public final class RetryTemplate {

    private RetryTemplate() {
    }

    /**
     * Retry an operation with a specified interval and timeout
     *
     * @param interval      how long the wait between tries
     * @param timeout       how long to keep trying for
     * @param retryCallback the callback to retry
     */
    public static void retry(Long interval, Long timeout, RetryCallback retryCallback) {
        Long finishTime = System.currentTimeMillis() + timeout;

        try {
            while (System.currentTimeMillis() < finishTime) {
                if (retryCallback.execute()) {
                    return;
                }

                Thread.sleep(interval);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        throw new IllegalStateException(retryCallback.getFailureMessage());
    }

}
