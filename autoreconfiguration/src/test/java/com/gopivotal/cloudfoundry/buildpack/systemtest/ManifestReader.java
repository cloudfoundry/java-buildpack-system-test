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
 * {@link ManifestReader} parses a CloudFoundry <code>manifest.yml</code>.
 */
public class ManifestReader {

    private final String name;
    private final String memory;
    private final int instances;
    private final File path;
    private final String buildpack;

    @SuppressWarnings("rawtypes")
    public ManifestReader(String applicationPath) throws IOException {
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
     * Gets the application name.
     * 
     * @return the application name
     */
    public String getName() {
	return this.name;
    }

    /**
     * Gets the memory size.
     * 
     * @return the memory size in string form including a unit such as 'm'
     */
    public String getMemory() {
	return this.memory;
    }

    /**
     * Gets the number of instances.
     * 
     * @return the number of instances
     */
    public int getInstances() {
	return this.instances;
    }

    /**
     * Gets the path of the application on the file system.
     * 
     * @return a {@link File} the path of the application
     */
    public File getPath() {
	return this.path;
    }

    /**
     * Gets the buildpack for the application.
     * 
     * @return the string URL of the buildpack
     */
    public String getBuildpack() {
	return this.buildpack;
    }

}
