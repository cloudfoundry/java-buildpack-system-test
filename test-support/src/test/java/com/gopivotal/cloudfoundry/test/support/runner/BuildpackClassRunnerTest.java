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

package com.gopivotal.cloudfoundry.test.support.runner;

import com.gopivotal.cloudfoundry.test.support.application.Application;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class BuildpackClassRunnerTest {

    @Test
    public void computeTestMethods() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(TestMethods.class);
        List<FrameworkMethod> methods = classRunner.computeTestMethods(Arrays.asList("alpha", "bravo"));

        assertEquals(2, methods.size());
        assertEquals("alpha", ((ApplicationSpecificFrameworkMethod) methods.get(0)).getApplication());
        assertEquals("bravo", ((ApplicationSpecificFrameworkMethod) methods.get(1)).getApplication());
    }

    @Test
    public void isTestMethodIgnoredIgnore() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(IgnoredTestMethods.class);
        FrameworkMethod method = new ApplicationSpecificFrameworkMethod("java-main",
                IgnoredTestMethods.class.getMethod("ignored", Application.class));

        assertTrue(classRunner.isTestMethodIgnored(method));
    }

    @Test
    public void isTestMethodIgnoredExcludedApplications() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(IgnoredTestMethods.class);
        FrameworkMethod method = new ApplicationSpecificFrameworkMethod("java-main",
                IgnoredTestMethods.class.getMethod("excluded", Application.class));

        assertTrue(classRunner.isTestMethodIgnored(method));
    }

    @Test
    public void isTestMethodIgnoredExcludedClassApplications() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(IgnoredTestMethods.class);
        FrameworkMethod method = new ApplicationSpecificFrameworkMethod("grails",
                IgnoredTestMethods.class.getMethod("excluded", Application.class));

        assertTrue(classRunner.isTestMethodIgnored(method));
    }

    @Test
    public void isTestMethodIgnoredExcludedApplicationsNotExcluded() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(IgnoredTestMethods.class);
        FrameworkMethod method = new ApplicationSpecificFrameworkMethod("web", IgnoredTestMethods.class.getMethod
                ("excluded", Application.class));

        assertFalse(classRunner.isTestMethodIgnored(method));
    }

    @Test
    public void getDescription() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(TestMethods.class);
        FrameworkMethod method = new ApplicationSpecificFrameworkMethod("web", TestMethods.class.getMethod
                ("test", Application.class));

        assertEquals("test#web(com.gopivotal.cloudfoundry.test.support.runner.BuildpackClassRunnerTest$TestMethods)",
                classRunner.describeChild(method).getDisplayName());
    }

    @Test
    public void methodInvoker() throws Exception {
        BuildpackClassRunner classRunner = new BuildpackClassRunner(TestMethods.class);
        Method method = TestMethods.class.getMethod("test", Application.class);

        Statement statement = classRunner.methodInvoker(new ApplicationSpecificFrameworkMethod("test-name",
                method), new TestMethods());

        assertTrue(statement instanceof MethodInvoker);
    }

    @Test(expected = InitializationError.class)
    public void validateTestMethodsInvalid() throws Exception {
        new BuildpackClassRunner(InvalidTestMethods.class);
    }

    public static final class TestMethods {

        public TestMethods() {
        }

        @SuppressWarnings("UnusedParameters")
        @Test
        public void test(Application application) {
        }

    }

    @ExcludedApplications("grails")
    public static final class IgnoredTestMethods {

        public IgnoredTestMethods() {
        }

        @Ignore
        @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
        @Test
        public void ignored(Application application) {
        }

        @ExcludedApplications("java-main")
        @SuppressWarnings({"UnusedParameters", "UnusedDeclaration"})
        @Test
        public void excluded(Application application) {
        }

    }

    public static final class InvalidTestMethods {

        public InvalidTestMethods() {
        }

        @Test
        public void test() {
        }
    }
}
