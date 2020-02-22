/* =========================================================================
 * File: $Id: $CacheException.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j;


public class CacheException extends Exception {

    private static final long serialVersionUID = -6129426355814347206L;

    public CacheException() {}

    public CacheException(String msg) {
        super(msg);
    }
}

