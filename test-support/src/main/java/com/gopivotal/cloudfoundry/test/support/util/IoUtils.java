// Copyright (c) 2013 Pivotal, Inc.  All rights reserved.
package com.gopivotal.cloudfoundry.test.support.util;

import java.io.Closeable;

/**
 * TODO Document IoUtils
 */
public class IoUtils {

    public static void closeQuietly(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    public static void deleteQuietly(Deletable... deletables) {
        for (Deletable deletable : deletables) {
            if(deletable != null) {
                try {
                    deletable.delete();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

}
