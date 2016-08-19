package ru.sbt.cache_proxy.main;

import java.util.Date;

/**
 * Created by kirill on 19.08.16
 */
public class CacheProxy {
    private final String rootPath;
    private final Date expired;

    public CacheProxy(String rootPath, Date expired) {
        this.rootPath = rootPath;
        this.expired = expired;
    }

    public <T> T cache(Object delegate) {
        return (T) new CachedInvocationHandler(delegate).cache(delegate);
    }
}
