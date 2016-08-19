package ru.sbt.cache_proxy.main;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static java.lang.ClassLoader.getSystemClassLoader;

/**
 * Created by kirill on 19.08.16
 */
public class CachedInvocationHandler implements InvocationHandler {
    private final Map<Object, Object> inMemoryStorage = new HashMap<>();
    private final Object delegate;

    public CachedInvocationHandler(Object delegate) {
        this.delegate = delegate;
    }

    public <T> T cache(Object delegate) {
        return (T) Proxy.newProxyInstance(getSystemClassLoader(),
                delegate.getClass().getInterfaces(),
                new CachedInvocationHandler(delegate)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(delegate, args);
    }
}
