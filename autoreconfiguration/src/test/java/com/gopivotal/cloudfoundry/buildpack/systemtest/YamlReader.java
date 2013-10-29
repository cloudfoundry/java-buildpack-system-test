// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * TODO Document YamlReader
 */
public class YamlReader {

    private final String name;
    private final String memory;
    private final int instances;
    private final File path;
    private final String buildpack;

    public static void main(String[] args) throws IOException {
	YamlReader yamlReader = new YamlReader("../vendor/java-test-applications/web-application");
	System.out.println(yamlReader.getName());
    }

    @SuppressWarnings("rawtypes")
    public YamlReader(String applicationPath) throws IOException {
	InputStream yamlStream = new FileInputStream(applicationPath + File.separator + "manifest.yml");
	try {
	    Yaml yaml = new Yaml();
	    Map payload = (Map) yaml.load(yamlStream);
	    Map application = (Map) ((List) payload.get("applications")).get(0);
	    this.name = (String) application.get("name");
	    this.memory = (String) application.get("memory");
	    this.instances = (Integer) application.get("instances"); // Integer.decode((String)
								     // application.get("instances"));
	    this.path = new File(applicationPath + File.separator + (String) application.get("path"));
	    this.buildpack = (String) application.get("buildpath");
	} finally {
	    yamlStream.close();
	}
    }

    /**
     * @return
     */
    public String getName() {
	return this.name;
    }

    /**
     * @return
     */
    public String getMemory() {
	return this.memory;
    }

    /**
     * @return
     */
    public int getInstances() {
	return this.instances;
    }

    /**
     * @return
     */
    public File getPath() {
	return this.path;
    }

    /**
     * @return
     */
    public String getBuildpack() {
	return this.buildpack;
    }

}
