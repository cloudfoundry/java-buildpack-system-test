// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * {@link ManifestReader} parses a CloudFoundry <code>manifest.yml</code>.
 */
public class ManifestReader implements Manifest {

    private static final String MEMORY_SYNTAX_MESSAGE_FORMAT = "Memory size from manifest.yml has bad syntax: '%s'";

    private final String name;
    private final int memory;
    private final int instances;
    private final File path;
    private final String buildpack;

    @SuppressWarnings("rawtypes")
    public ManifestReader(String applicationPath) {
	InputStream yamlStream = null;
	try {
	    yamlStream = new FileInputStream(applicationPath + File.separator + "manifest.yml");
	    Yaml yaml = new Yaml();
	    Map payload = (Map) yaml.load(yamlStream);
	    Map application = (Map) ((List) payload.get("applications")).get(0);
	    this.name = (String) application.get("name");
	    this.memory = toMegabytes((String) application.get("memory"));
	    this.instances = (Integer) application.get("instances"); // Integer.decode((String)
								     // application.get("instances"));
	    this.path = new File(applicationPath + File.separator + (String) application.get("path"));
	    this.buildpack = (String) application.get("buildpath");
	} catch (FileNotFoundException e) {
	    throw new RuntimeException(e);
	} finally {
	    IoUtils.closeQuietly(yamlStream);
	}
    }

    private int toMegabytes(String memory) {
	if (memory.length() < 2) {
	    throwMemorySyntaxException(memory);
	}
	String unit = memory.substring(memory.length() - 1).toLowerCase();
	String valueString = memory.substring(0, memory.length() - 1);
	int value = -1;
	try {
	    value = Integer.parseInt(valueString);
	} catch (NumberFormatException e) {
	    throwMemorySyntaxException(memory, e);
	}
	if (value <= 0) {
	    throwMemorySyntaxException(memory);
	}
	if ("m".equals(unit)) {
	    return value;
	} else if ("g".equals(unit)) {
	    return 1024 * value;
	}
	throwMemorySyntaxException(memory);
	return -1; // never executed
    }

    private void throwMemorySyntaxException(String memory) {
	throw new IllegalArgumentException(String.format(MEMORY_SYNTAX_MESSAGE_FORMAT, memory));
    }

    private void throwMemorySyntaxException(String memory, Exception e) {
	throw new IllegalArgumentException(String.format(MEMORY_SYNTAX_MESSAGE_FORMAT, memory), e);
    }

    /**
     * Gets the application name.
     * 
     * @return the application name
     */
    @Override
    public String getName() {
	return this.name;
    }

    /**
     * Gets the memory size.
     * 
     * @return the memory size in string form including a unit such as 'm'
     */
    @Override
    public int getMemory() {
	return this.memory;
    }

    /**
     * Gets the number of instances.
     * 
     * @return the number of instances
     */
    @Override
    public int getInstances() {
	return this.instances;
    }

    /**
     * Gets the path of the application on the file system.
     * 
     * @return a {@link File} the path of the application
     */
    @Override
    public File getPath() {
	return this.path;
    }

    /**
     * Gets the buildpack for the application.
     * 
     * @return the string URL of the buildpack
     */
    @Override
    public String getBuildpack() {
	return this.buildpack;
    }

}
