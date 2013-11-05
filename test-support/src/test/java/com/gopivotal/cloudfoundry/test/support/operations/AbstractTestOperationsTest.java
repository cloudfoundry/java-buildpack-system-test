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

package com.gopivotal.cloudfoundry.test.support.operations;

import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public final class AbstractTestOperationsTest {

    private final AbstractTestOperations testOperations = mock(AbstractTestOperations.class);

    public AbstractTestOperationsTest() {
        mockTestOperations(this.testOperations);
    }

    private static void mockTestOperations(TestOperations testOperations) {
        when(testOperations.classPath()).thenReturn(Arrays.asList("/directory/alpha.jar", "/directory/bravo.jar"));
    }

    @Test
    public void isJarOnClassPathTrue() throws Exception {
        assertTrue(this.testOperations.isJarOnClassPath("bravo"));
    }

    @Test
    public void isJarOnClassPathFalse() throws Exception {
        assertFalse(this.testOperations.isJarOnClassPath("charlie"));
    }
}
