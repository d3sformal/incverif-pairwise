/* =========================================================================
 * File: $Id: $ManagedCache.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j;

import net.sf.cache4j.CacheException;
import net.sf.cache4j.CacheConfig;

public interface ManagedCache {
    public void setCacheConfig(CacheConfig config) throws CacheException;
    public void clean() throws CacheException;
}

