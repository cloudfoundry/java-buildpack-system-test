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
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BuildpackClassRunner extends SpringJUnit4ClassRunner {

    private static final Pattern NAME_PATTERN = Pattern.compile("([\\w\\-]*)-application$");

    public BuildpackClassRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        return computeTestMethods(getApplicationNames());
    }

    List<FrameworkMethod> computeTestMethods(List<String> applicationNames) {
        List<FrameworkMethod> testMethods = super.computeTestMethods();

        List<FrameworkMethod> applicationMethods = new ArrayList<>(testMethods.size() * applicationNames.size());
        for (FrameworkMethod method : testMethods) {
            for (String applicationName : applicationNames) {
                applicationMethods.add(new ApplicationSpecificFrameworkMethod(applicationName, method.getMethod()));
            }
        }

        return applicationMethods;
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
        return Description.createTestDescription(getTestClass().getJavaClass(), getTestName(method),
                method.getAnnotations());
    }

    @Override
    protected boolean isTestMethodIgnored(FrameworkMethod frameworkMethod) {
        return super.isTestMethodIgnored(frameworkMethod) || isExcludedApplication(frameworkMethod);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        MethodInvoker methodInvoker = new MethodInvoker(method, test, getApplicationName(method));
        getTestContextManager().registerTestExecutionListeners(methodInvoker);
        return methodInvoker;
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
        for (FrameworkMethod method : getTestClass().getAnnotatedMethods(Test.class)) {
            if (!hasValidParameters(method)) {
                errors.add(new Exception(String.format("Method %s should have a single parameter of type %s",
                        method.getName(), Application.class.getName())));
            }
        }
    }

    private List<String> getApplicationNames() {
        List<String> applicationNames = new ArrayList<>();

        File[] candidates = new File("../vendor/java-test-applications").listFiles();
        if (candidates != null) {
            for (File candidate : candidates) {
                Matcher matcher = NAME_PATTERN.matcher(candidate.getName());

                if (candidate.isDirectory() && matcher.find()) {
                    applicationNames.add(matcher.group(1));
                }
            }
        }

        return applicationNames;
    }

    private String getApplicationName(FrameworkMethod method) {
        return ((ApplicationSpecificFrameworkMethod) method).getApplication();
    }

    private String getTestName(FrameworkMethod method) {
        return String.format("%s#%s", testName(method), getApplicationName(method));
    }

    private Set<String> getExcludedApplicationNames(Method method) {
        Set<String> names = new HashSet<>();

        names.addAll(nullSafeValue(AnnotationUtils.findAnnotation(method, ExcludedApplications.class)));
        names.addAll(nullSafeValue(AnnotationUtils.findAnnotation(method.getDeclaringClass(),
                ExcludedApplications.class)));

        return names;
    }

    private boolean hasValidParameters(FrameworkMethod method) {
        Class<?>[] parameterTypes = method.getMethod().getParameterTypes();
        return parameterTypes.length == 1 && Application.class.isAssignableFrom(parameterTypes[0]);
    }

    private boolean isExcludedApplication(FrameworkMethod frameworkMethod) {
        ApplicationSpecificFrameworkMethod method = (ApplicationSpecificFrameworkMethod) frameworkMethod;
        return getExcludedApplicationNames(method.getMethod()).contains(method.getApplication());
    }

    private List<String> nullSafeValue(ExcludedApplications annotation) {
        return annotation == null ? Collections.<String>emptyList() : Arrays.asList(annotation.value());
    }

}
