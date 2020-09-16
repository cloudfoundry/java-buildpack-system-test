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

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.organizations.CreateOrganizationRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.spaces.CreateSpaceRequest;
import org.cloudfoundry.operations.spaces.SpaceSummary;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

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
    @DependsOn({"organization", "space"})
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
    Mono<Void> organization(CloudFoundryClient cloudFoundryClient,
                            UaaClient uaaClient,
                            @Value("${test.organization}") String organization) {

        CloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient)
            .uaaClient(uaaClient)
            .build();

        return cloudFoundryOperations.organizations()
            .list()
            .filter(o -> organization.equals(o.getName()))
            .switchIfEmpty(cloudFoundryOperations.organizations()
                .create(CreateOrganizationRequest.builder()
                    .organizationName(organization)
                    .build())
                .cast(OrganizationSummary.class))
            .then();
    }

    @Bean(initMethod = "block")
    @DependsOn("organization")
    Mono<Void> space(CloudFoundryClient cloudFoundryClient,
                     UaaClient uaaClient,
                     @Value("${test.organization}") String organization,
                     @Value("${test.space}") String space) {

        CloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient)
            .uaaClient(uaaClient)
            .organization(organization)
            .build();

        return cloudFoundryOperations.spaces()
            .list()
            .filter(s -> space.equals(s.getName()))
            .switchIfEmpty(cloudFoundryOperations.spaces()
                .create(CreateSpaceRequest.builder()
                    .name(space)
                    .build())
                .cast(SpaceSummary.class))
            .then();
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
    UaaClient uaaClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorUaaClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    WebClient webClient(@Value("${test.skipSslValidation:false}") Boolean skipSslValidation) throws SSLException {
        if (!skipSslValidation) {
            return WebClient.create();
        }

        SslContext context = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create().secure(s -> s.sslContext(context))))
            .build();
    }

}
