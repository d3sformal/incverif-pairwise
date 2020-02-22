/* =========================================================================
 * File: CacheInfo.java$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j;

public interface CacheInfo {
    public long getCacheHits();
    public long getCacheMisses();
    public long getTotalPuts();
    public long getTotalRemoves();
    public void reset();
    public long getMemorySize();
}

