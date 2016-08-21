package ru.sbt.cache_proxy.main;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.annotations.CacheType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static java.lang.ClassLoader.getSystemClassLoader;
import static ru.sbt.cache_proxy.main.CacheProxyUtils.*;
import static ru.sbt.cache_proxy.serialization.SerializationUtils.key;

/**
 * Created by kirill on 19.08.16
 */
public class CachedInvocationHandler implements InvocationHandler {
    private final Map<Object, Object> inMemoryStorage = new HashMap<>();
    private final Object delegate;
    private final String rootPath;

    public CachedInvocationHandler(Object delegate, String rootPath) {
        this.delegate = delegate;
        this.rootPath = rootPath;
    }

    public <T> T cache(Object delegate) {
        return (T) Proxy.newProxyInstance(getSystemClassLoader(),
                delegate.getClass().getInterfaces(),
                new CachedInvocationHandler(delegate, rootPath)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!method.isAnnotationPresent(Cache.class)) return CacheProxyUtils.invoke(method, args, delegate);

        Cache cache = method.getAnnotation(Cache.class);

        Object result = null;
        if (cache != null) {
            if (cache.cacheType().equals(CacheType.IN_MEMORY))
                result = inMemoryProcess(method, args);
            else if (cache.cacheType().equals(CacheType.FILE))
                result = fileProcess(method, args);
        }

        return result;
    }

    private Object inMemoryProcess(Method method, Object[] args) throws Throwable {
        Object[] identityArgs = getIdentityArguments(method, args);
        if (!inMemoryStorage.containsKey(key(method, args))) {
            Object result = CacheProxyUtils.invoke(method, args, delegate);
            result = checkItemsAmountToCache(method, result);
            inMemoryStorage.put(key(method, identityArgs), result);
        }

        return inMemoryStorage.get(key(method, args));
    }

    private Object fileProcess(Method method, Object[] args) throws Throwable {
        Object[] identityArguments = getIdentityArguments(method, args);
        String fileName = generateFileName(method, identityArguments, rootPath);
        Object result = null;
        String zipFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".zip";
        if (!new File(fileName).exists() && !new File(zipFileName).exists()) {
            try {
                serialize(method, args, delegate, rootPath);
            } catch (IOException e) {
                throw new RuntimeException("Exception happened while working with file " + fileName, e);
            }
        } else {
            try {
                result = deserialize(method, args, rootPath);
            } catch (IOException e) {
                throw new RuntimeException("Exception happened while trying to deserialize file " + fileName, e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class was not found. Check your file name.", e);
            }
        }

        return result;
    }

}
