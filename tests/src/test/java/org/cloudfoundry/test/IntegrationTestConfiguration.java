/*
 * Copyright 2013-2019 the original author or authors.
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

package org.cloudfoundry.test;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.organizations.CreateOrganizationRequest;
import org.cloudfoundry.operations.spaces.CreateSpaceRequest;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ComponentScan
@Configuration
@EnableAutoConfiguration
public class IntegrationTestConfiguration {

    @Bean
    String buildpack(@Value("${test.buildpack}") String buildpack) {
        return buildpack;
    }

    @Bean
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    CloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient,
                                                  DopplerClient dopplerClient,
                                                  @Value("${test.organization}") String organization,
                                                  @Value("${test.space}") String space) {

        return DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient)
            .dopplerClient(dopplerClient)
            .organization(organization)
            .space(space)
            .build();
    }

    @Bean
    DefaultConnectionContext connectionContext(@Value("${test.host}") String host,
                                               @Value("${test.skipSslValidation:false}") Boolean skipSslValidation) {

        return DefaultConnectionContext.builder()
            .apiHost(host)
            .skipSslValidation(skipSslValidation)
            .build();
    }

    @Bean
    ReactorDopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean(initMethod = "block")
    Mono<Void> organization(CloudFoundryOperations cloudFoundryOperations,
                            @Value("${test.organization}") String organization) {

        return cloudFoundryOperations.organizations()
            .create(CreateOrganizationRequest.builder()
                .organizationName(organization)
                .build());
    }

    @Bean(initMethod = "block")
    @DependsOn("organization")
    Mono<Void> space(CloudFoundryOperations cloudFoundryOperations,
                     @Value("${test.space}") String space) {

        return cloudFoundryOperations.spaces()
            .create(CreateSpaceRequest.builder()
                .name(space)
                .build());
    }

    @Bean
    PasswordGrantTokenProvider tokenProvider(@Value("${test.username}") String username,
                                             @Value("${test.password}") String password) {
        return PasswordGrantTokenProvider.builder()
            .username(username)
            .password(password)
            .build();
    }

    @Bean
    WebClient webClient() {
        return WebClient.create();
    }

}
