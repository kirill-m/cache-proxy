package ru.sbt.cache_proxy.main;

/**
 * Created by kirill on 19.08.16
 */
public class CacheProxy {
    private final String rootPath;

    public CacheProxy(String rootPath) {
        this.rootPath = rootPath;
    }

    public <T> T cache(Object delegate) {
        return (T) new CachedInvocationHandler(delegate, rootPath).cache(delegate);
    }
}
