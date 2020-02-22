/* =========================================================================
 * File: $Id: $CacheConfig.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j;

public interface CacheConfig {

	public Object getCacheId();

    public String getCacheDesc();

    public long getTimeToLive();

    public long getIdleTime();

    public long getMaxMemorySize();

    public int getMaxSize();

    /**
     *  <ul>
     *   <li>blocking - {@link net.sf.cache4j.impl.BlockingCache}</li>
     *   <li>synchronized - {@link net.sf.cache4j.impl.SynchronizedCache}</li>
     *   <li>nocache - {@link net.sf.cache4j.impl.EmptyCache}</li>
     *  </ul>
     */
    public String getType();

    /**
     * <ul>
     *   <li>lru (Least Recently Used)</li>
     *   <li>lfu (Least Frequently Used)</li>
     *   <li>fifo (First In First Out)</li>
     * </ul>
     */
    public String getAlgorithm();

    /**
     * <ul>
     *   <li>strong</li>
     *   <li>soft - <code>java.lang.ref.SoftReference</code></li>
     * </ul>
     */
    public String getReference();
}

