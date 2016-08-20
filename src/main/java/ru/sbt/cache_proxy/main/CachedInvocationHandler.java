package ru.sbt.cache_proxy.main;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.annotations.CacheType;
import ru.sbt.cache_proxy.domain.Result;
import ru.sbt.cache_proxy.serialization.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.Arrays.asList;

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
        if (!method.isAnnotationPresent(Cache.class)) return invoke(method, args);

        Cache cache = method.getAnnotation(Cache.class);

        Object result = null;
        if (cache != null) {
            if (cache.cacheType().equals(CacheType.IN_MEMORY))
                result =  inMemoryProcess(method, args);
            else if (cache.cacheType().equals(CacheType.FILE))
                result =  fileProcess(method, args);
        }

        return result;
    }


    private Object invoke(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private Object key(Method method, Object[] args) {
        List<Object> key = new ArrayList<>();
        key.add(method);
        key.addAll(asList(args));
        return key;
    }

    private Object inMemoryProcess(Method method, Object[] args) throws Throwable {
        if (!inMemoryStorage.containsKey(key(method, args))) {
            System.out.println("Delegation of " + method.getName());
            Object result = invoke(method, args);
            inMemoryStorage.put(key(method, args), result);
        }
        return inMemoryStorage.get(key(method, args));

    }

    private Object fileProcess(Method method, Object[] args) throws Throwable {
        String fileName = generateFileName(method, args);
        Object result = null;
        if (!new File(fileName).exists()) {
            System.out.println("File has been created");
            try {
                result = invoke(method, args);
                System.out.println("result: " + result);
                SerializationUtils.serialize(new Result(result), fileName);
            } catch (IOException e) {
                throw new RuntimeException("Exception happened while working with file " + fileName, e);
            }
        } else {
            System.out.println("File exists");
            try {
                Result myResult = SerializationUtils.deserialize(fileName);
                result = myResult.getResult();
                System.out.println("Result: " + myResult.getResult());
            } catch (IOException e) {
                throw new RuntimeException("Exception happened while trying to deserialize file " + fileName, e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class was not found. Check your file name.", e);
            }
        }

        return result;
    }

    private String generateFileName(Method method, Object[] args) {
        StringBuilder sb =  new StringBuilder();
        sb.append(method.getName())
                .append("_");

        for (Object arg : args) {
            sb.append(arg).append("_");
        }
        sb.replace(sb.lastIndexOf("_"), sb.lastIndexOf("_"), ".ser");

        String fileName = sb.toString();
        fileName = fileName.substring(0, fileName.lastIndexOf("_"));

        return rootPath + fileName;
    }
}
