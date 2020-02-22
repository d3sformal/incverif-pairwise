/* =========================================================================
 * File: Tester.java$
 *
 * Copyright 2006 by Yuriy Stepovoy.
 * email: stepovoy@gmail.com
 * All rights reserved.
 *
 * =========================================================================
 */

package net.sf.cache4j.test;

import net.sf.cache4j.CacheConfig;
import net.sf.cache4j.CacheException;
import net.sf.cache4j.Cache;
import net.sf.cache4j.impl.BlockingCache;
import net.sf.cache4j.impl.CacheConfigImpl;

import java.util.Random;

public class Tester {

    public static void main(String[] args) {

        try {
	      Tester.runTest();

        } catch (Exception e){
            e.printStackTrace();
            return;
        }
    }

    private static void runTest(){

        BlockingCache cache = new BlockingCache();
        CacheConfig cacheConfig = new CacheConfigImpl("cacheId", null, 0, 0, 0, 1000, null, "lru", "strong");
        try {
            cache.setCacheConfig(cacheConfig);
        } catch (CacheException e) {
            throw new RuntimeException(e);
        }

        final int tcount = 2;
        final int count = 4;

        Thread[] tthreads = new Thread[tcount];
        for (int i = 0; i < tcount; i++) {
            tthreads[i] = new TThread(cache, count);
            tthreads[i].start();
        }

  
        for (int i = 0; i < tcount; i++) {
            try {
                tthreads[i].join();
            } catch(InterruptedException e) {}
        }

    }


    static class TThread extends Thread {
        final private Cache _cache;
        final private long _count;
        final private Random _rnd = new Random();

        public TThread(Cache cache, long count) {
            _cache = cache;
            _count = count;
        }

        public void run() {
            long count = 0;
            try {
                while(count < _count){
                    count++;
                    Object key = new Long(_rnd.nextInt(10));
                    _cache.get(key);
                    _cache.put(key, key);
                    //_cache.remove(key);
                }
            } catch (Exception e){
                throw new RuntimeException(e.getMessage());
            }
        }
    }


    private static void log(String s){
        System.out.println(s);
    }

}

