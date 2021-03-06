/* =========================================================================
 * File: $Id: $BlockingCache.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j.impl;

import net.sf.cache4j.CacheException;
import net.sf.cache4j.Cache;
import net.sf.cache4j.CacheConfig;
import net.sf.cache4j.CacheInfo;
import net.sf.cache4j.ManagedCache;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.io.IOException;

public class BlockingCache implements Cache, ManagedCache {

    private Map _map;

    private TreeMap _tmap;

    private CacheConfigImpl _config;

    private long _memorySize;

    private CacheInfoImpl _cacheInfo;

    private ThreadLocal _tl = new ThreadLocal();

    
    public void put(Object objId, Object obj) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject tlCO = (CacheObject)_tl.get();
        CacheObject co = null;

        int objSize = 0;

        checkOverflow(objSize);

        if (tlCO==null) {
            co = getCacheObject(objId);
        } else {
            if(tlCO.getObjectId().equals(objId)){
                co = tlCO;
                _tl.set(null);
            } else {
                tlCO.unlock();
                throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
            }
        }

        co.lock();
        _cacheInfo.incPut();
        try {
            tmapRemove(co);

            resetCacheObject(co);

            co.setObject(obj);
            co.setObjectSize(objSize);

			// INJECTED ERROR
            //synchronized(this) {
                _memorySize = _memorySize + objSize;
            //}

            tmapPut(co);
        } finally {
            co.unlock();
        }
    }

    public Object get(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject tlCO = (CacheObject)_tl.get();
        if (tlCO!=null) {
            throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
        }

        CacheObject co = getCacheObject(objId);
        co.lock();
        Object o = co.getObject();
        if(o!=null){
            tmapRemove(co);

            if(!valid(co)) {
                resetCacheObject(co);
                _tl.set(co);

                _cacheInfo.incMisses();
                return null;
            } else {
                co.updateStatistics();
                tmapPut(co);

                co.unlock();
                _cacheInfo.incHits();

                //?:return copy(o);
                return o;
            }
        } else {
            _tl.set(co);
            _cacheInfo.incMisses();
            return null;
        }
    }

    public void remove(Object objId) throws CacheException {
        if(objId==null) {
            throw new NullPointerException("objId is null");
        }

        CacheObject tlCO = (CacheObject)_tl.get();
        if (tlCO!=null) {
            throw new CacheException("Cache:"+_config.getCacheId()+" wait for call put() with objId="+tlCO.getObjectId());
        }

        CacheObject co = null;//getCacheObject(objId);
        synchronized (this) {
            co = (CacheObject)_map.get(objId);
        }

        if (co==null) {
            return;
        }

        co.lock();
        _cacheInfo.incRemove();
        try {
            synchronized (this) {
                tmapRemove(co);

                CacheObject co2 = (CacheObject)_map.get(co.getObjectId());
                if(co2!=null && co2==co){
                    _map.remove(co.getObjectId());
                    resetCacheObject(co);
                }
            }
        } finally {
            co.unlock();
        }
    }

    public int size() {
        return _map.size();
    }

    public void clear() throws CacheException {
        Object[] objArr = null;
        synchronized (this) {
            objArr = _map.values().toArray();
        }

        for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            remove(((CacheObject)objArr[i]).getObjectId());
        }
    }

    public CacheInfo getCacheInfo() {
        return _cacheInfo;
    }

    public CacheConfig getCacheConfig() {
        return _config;
    }

    public synchronized void setCacheConfig(CacheConfig config) throws CacheException {
        if(config==null) {
            throw new NullPointerException("config is null");
        }

        _config = (CacheConfigImpl)config;

        if (_config.getMaxSize()>1000) _map = new HashMap(1024);
		else _map = new HashMap();
        _memorySize = 0;
        _tmap = new TreeMap(_config.getAlgorithmComparator());
        _cacheInfo = new CacheInfoImpl();
        _tl.set(null);
    }

    public void clean() throws CacheException {
        if(_config.getTimeToLive()==0 && _config.getIdleTime()==0){
            return;
        }

        Object[] objArr = null;
        synchronized (this) {
            objArr = _map.values().toArray();
        }

        for (int i = 0, indx = objArr==null ? 0 : objArr.length; i<indx; i++) {
            CacheObject co = (CacheObject)objArr[i];
            if ( !valid(co) ) {
                remove(co.getObjectId());
            }
        }
    }

    private void checkOverflow(int objSize) {
        synchronized(this) {
            while( (_config.getMaxSize() > 0 &&  _map.size()+1>_config.getMaxSize() ) ||
                    (_config.getMaxMemorySize()  > 0 && _memorySize+objSize > _config.getMaxMemorySize())  ) {

                Object firstKey = null;
				if (_tmap.size()==0) firstKey = null;
				else firstKey = _tmap.firstKey();
                CacheObject fco = null;
				if (firstKey==null) fco = null;
				else fco = (CacheObject)_tmap.remove(firstKey);

                if(fco!=null) {
                    CacheObject co = (CacheObject)_map.get(fco.getObjectId());
                    if(co!=null && co==fco){
                        _map.remove(fco.getObjectId());
                        resetCacheObject(fco);
                    }
                }
            } //while
        }
    }

    private void tmapRemove(CacheObject co){
        synchronized(this) {
            _tmap.remove(co);
        }
    }

    private void tmapPut(CacheObject co){
        synchronized(this) {
            Object mapO = _map.get(co.getObjectId());
            if(mapO!=null && mapO==co){
                _tmap.put(co, co);
            }
        }
    }


    private CacheObject getCacheObject(Object objId) {
        synchronized (this) {
            CacheObject co = (CacheObject)_map.get(objId);
            if (co == null) {
                co = _config.newCacheObject(objId);
                _map.put(objId, co);
            }
            return co;
        }
    }

    private boolean valid(CacheObject co){
        // NOTE 
		// we do not need to model time since it is only a local variable and it is not stored anywhere
		// moreover, this method always returns "true" because TTL and IdleTime are 0.
		
		return true;

        //long curTime = System.currentTimeMillis(); 
        //long curTime = 1000;
        //return  (_config.getTimeToLive()==0 || (co.getCreateTime() + _config.getTimeToLive()) >= curTime) &&
        //        (_config.getIdleTime()==0 || (co.getLastAccessTime() + _config.getIdleTime()) >= curTime) &&
        //        co.getObject()!=null;
    }

    private void resetCacheObject(CacheObject co){
        synchronized(this) {
            _memorySize = _memorySize - co.getObjectSize();
        }
        co.reset();
    }

    private class CacheInfoImpl implements CacheInfo {
        private long _hit;
        private long _miss;
        private long _put;
        private long _remove;

        synchronized void incHits(){
            _hit++;
        }
        synchronized void incMisses(){
            _miss++;
        }
        synchronized void incPut(){
            _put++;
        }
        synchronized void incRemove(){
            _remove++;
        }
        public long getCacheHits(){
            return _hit;
        }
        public long getCacheMisses(){
            return _miss;
        }
        public long getTotalPuts() {
            return _put;
        }
        public long getTotalRemoves() {
            return _remove;
        }
        public synchronized void reset() {
            _hit = 0;
            _miss = 0;
            _put = 0;
            _remove = 0;
        }
        public long getMemorySize() {
            return _memorySize;
        }
        public String toString(){
            return "hit:"+_hit+" miss:"+_miss+" memorySize:"+_memorySize;
            //return "hit:"+_hit+" miss:"+_miss+" memorySize:"+_memorySize+" size:"+_map.size()+" tsize:"+_tmap.size();
        }
    }
}

