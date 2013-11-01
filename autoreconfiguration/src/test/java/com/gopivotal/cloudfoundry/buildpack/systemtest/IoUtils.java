// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.buildpack.systemtest;

import java.io.Closeable;
import java.io.IOException;

/**
 * TODO Document IoUtils
 */
public class IoUtils {

    public static void closeQuietly(Closeable... closeables) {
	for (Closeable closeable : closeables) {
	    if (closeable != null) {
		try {
		    closeable.close();
		} catch (IOException e) {
		    // ignore
		}
	    }
	}
    }

}
