/* =========================================================================
 * File: $Id: SoftCacheObject.java,v$
 *
 * Copyright (c) 2006, Yuriy Stepovoy. All rights reserved.
 * email: stepovoy@gmail.com
 *
 * =========================================================================
 */

package net.sf.cache4j.impl;

import java.lang.ref.SoftReference;

public class SoftCacheObject extends CacheObject {

    SoftCacheObject(Object objId) {
        super(objId);
    }

    Object getObject() {
        if (_obj==null) return null;
		else return ((SoftReference)_obj).get();
    }

    void setObject(Object obj) {
		if (obj==null) _obj = null;
		else _obj = new SoftReference(obj);
    }
}

