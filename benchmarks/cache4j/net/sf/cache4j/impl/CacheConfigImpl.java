/* =========================================================================
 * File: CacheConfigImpl.java$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j.impl;

import net.sf.cache4j.CacheConfig;

import java.util.Comparator;

public class CacheConfigImpl implements CacheConfig {

    private Object _cacheId;
    private String _cacheDesc;
    private long _ttl;
    private long _idleTime;
    private long _maxMemorySize;
    private int _maxSize;
    private String _type;
    private String _algorithm;
    private String _reference;
    private int _referenceInt;

    static final String LRU = "lru";
    static final String LFU = "lfu";
    static final String FIFO = "fifo";

    static final int STRONG = 1;
    static final int SOFT = 2;

    public CacheConfigImpl(Object cacheId,
                           String cacheDesc,
                           long ttl,
                           long idleTime,
                           long maxMemorySize,
                           int maxSize,
                           String type,
                           String algorithm,
                           String reference) {
        _cacheId = cacheId;
        _cacheDesc = cacheDesc;
        _ttl = ttl<0 ? 0 : ttl;
        _idleTime = idleTime<0 ? 0 : idleTime;
        _maxMemorySize = maxMemorySize<0 ? 0 : maxMemorySize;
        _maxSize = maxSize<0 ? 0 : maxSize;
        _type = type;
        _algorithm = algorithm;
        _reference = reference;

        if(_reference.equalsIgnoreCase("strong")){
            _referenceInt = STRONG;
        } else if(_reference.equalsIgnoreCase("soft")){
            _referenceInt = SOFT;
        }
    }

    public Object getCacheId() {
        return _cacheId;
    }
    public String getCacheDesc() {
        return _cacheDesc;
    }
    public long getTimeToLive() {
        return _ttl;
    }
    public long getIdleTime() {
        return _idleTime;
    }
    public long getMaxMemorySize() {
        return _maxMemorySize;
    }
    public int getMaxSize() {
        return _maxSize;
    }
    public String getType() {
        return _type;
    }
    public String getAlgorithm() {
        return _algorithm;
    }
    public String getReference(){
        return _reference;
    }

    CacheObject newCacheObject(Object objId){
        return _referenceInt==CacheConfigImpl.STRONG ? new CacheObject(objId) : new SoftCacheObject(objId);
    }

    Comparator getAlgorithmComparator(){
        if(_algorithm.equals(CacheConfigImpl.LRU)) {
            return new LRUComparator();
        } else {
            throw new RuntimeException("Unknown algorithm:"+_algorithm);
        }
    }
}

