/* =========================================================================
 * File: $Id: $Cache.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j;

public interface Cache {

	public void put(Object objId, Object obj) throws CacheException;

    public Object get(Object objId) throws CacheException;

    public void remove(Object objId) throws CacheException;

    public int size();

    public void clear() throws CacheException;

    public CacheConfig getCacheConfig();

    public CacheInfo getCacheInfo();
}

