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

package com.gopivotal.cloudfoundry.test.support.service;

import com.gopivotal.cloudfoundry.test.support.util.RandomizedNameFactory;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

abstract class AbstractService implements Service {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final CloudFoundryOperations cloudFoundryOperations;

    private final String name;

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected AbstractService(CloudFoundryOperations cloudFoundryOperations, String label, String plan,
                              RandomizedNameFactory randomizedNameFactory) {
        this.cloudFoundryOperations = cloudFoundryOperations;
        this.name = randomizedNameFactory.create(label);

        this.logger.info("Creating service {}", this.name);

        CloudServiceOffering cloudServiceOffering = getServiceOffering(cloudFoundryOperations, label);
        createCloudService(cloudFoundryOperations, cloudServiceOffering, this.name, label, plan);
    }

    private static CloudServiceOffering getServiceOffering(CloudFoundryOperations cloudFoundryOperations,
                                                           String label) {
        for (CloudServiceOffering cloudServiceOffering : cloudFoundryOperations.getServiceOfferings()) {
            if (label.equals(cloudServiceOffering.getLabel())) {
                return cloudServiceOffering;
            }
        }

        throw new IllegalArgumentException(String.format("No service type with a label of %s", label));
    }

    private static void createCloudService(CloudFoundryOperations cloudFoundryOperations,
                                           CloudServiceOffering cloudServiceOffering, String name, String label,
                                           String plan) {
        CloudService cloudService = new CloudService(CloudEntity.Meta.defaultMeta(), name);
        cloudService.setProvider("core");
        cloudService.setLabel(label);
        cloudService.setVersion(cloudServiceOffering.getVersion());
        cloudService.setPlan(plan);

        cloudFoundryOperations.createService(cloudService);
    }

    @Override
    public final String getName() {
        return this.name;
    }

    @Override
    public final void delete() {
        this.logger.debug("Deleting service {}", this.name);
        this.cloudFoundryOperations.deleteService(this.name);
    }

    @SuppressWarnings("unchecked")
    protected final Map<String, Object> getCredentials(Map<String, String> environmentVariables) {
        try {
            Map<String, List<Map<String, Object>>> vcapServices = this.objectMapper.readValue(environmentVariables.get
                    ("VCAP_SERVICES"), Map.class);
            Map<String, Object> servicePayload = getServicePayload(vcapServices);

            return (Map<String, Object>) servicePayload.get("credentials");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getServicePayload(Map<String, List<Map<String, Object>>> vcapServices) {
        for (List<Map<String, Object>> serviceTypePayload : vcapServices.values()) {
            for (Map<String, Object> servicePayload : serviceTypePayload) {
                if (this.name.equals(servicePayload.get("name"))) {
                    return servicePayload;
                }
            }
        }

        throw new IllegalStateException(String.format("Cannot find VCAP_SERVICES payload for service %s", this.name));
    }

    /**
     * Create a testable url suitable of the form &lt;scheme&gt;://&lt;host&gt;:&lt;port&gt;/&lt;path&gt;
     * from a url (typically created by accessing the "uri" attribute of a service data from the VCAP_SERVICE 
     * env variable) that might include user credentials and query params
     * 
     * @param url url that might include username and password (...://&lt;username&gt;@&lt;password&gt;...)
     *                and query parameters (.../path?&lt;query&gt;)
     * @return
     */
    protected final String getTestableUrl(String url) {
        int doubleSlashIndex = url.indexOf("//");
        if (doubleSlashIndex != -1) {
            int at = url.indexOf("@");
            if (at != -1) {
                url = url.substring(0, doubleSlashIndex + 2) + url.substring(at + 1);
            }
        }
        int queryIndex = url.indexOf('?');
        if (queryIndex != -1) {
            url = url.substring(0, queryIndex);
        }
        
        // Special processing for postgresql. The "uri" in the environment uses "postgres" as the scheme,
        // whereas the driver needs (and hence the endpoint returns) "postgresql"
        url = url.replaceFirst("postgres:", "postgresql:");
        
        return url;
    }
}
