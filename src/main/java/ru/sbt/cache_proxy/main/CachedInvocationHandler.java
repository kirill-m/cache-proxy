package ru.sbt.cache_proxy.main;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.annotations.CacheType;
import ru.sbt.cache_proxy.domain.Result;
import ru.sbt.cache_proxy.serialization.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
        if (!method.isAnnotationPresent(Cache.class)) return invoke(method, args);

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

    private Object invoke(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

//    private Object key(Method method, Object[] args) {
//        List<Object> key = new ArrayList<>();
//        key.add(method);
//        key.addAll(asList(args));
//
//        return key;
//    }

    private Object inMemoryProcess(Method method, Object[] args) throws Throwable {
        Object[] identityArgs = getIdentityArguments(method, args);
        if (!inMemoryStorage.containsKey(key(method, args))) {
            Object result = invoke(method, args);
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
                serialize(method, args);
            } catch (IOException e) {
                throw new RuntimeException("Exception happened while working with file " + fileName, e);
            }
        } else {
            try {
                result = deserialize(method, args);
            } catch (IOException e) {
                throw new RuntimeException("Exception happened while trying to deserialize file " + fileName, e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Class was not found. Check your file name.", e);
            }
        }

        return result;
    }

//    private String generateFileName(Method method, Object[] args) {
//        String prefix = method.getAnnotation(Cache.class).fileNamePrefix();
//        if ("default".equals(prefix))
//            prefix = method.getName();
//        StringBuilder sb = new StringBuilder();
//        sb.append(prefix)
//                .append("_");
//
//        for (Object arg : args) {
//            sb.append(arg).append("_");
//        }
//        sb.replace(sb.lastIndexOf("_"), sb.lastIndexOf("_"), ".ser");
//
//        String fileName = sb.toString();
//        fileName = fileName.substring(0, fileName.lastIndexOf("_"));
//
//        return rootPath + fileName;
//    }

//    private Object[] getIdentityArguments(Method method, Object[] args) {
//        Cache cache = method.getAnnotation(Cache.class);
//        Class<?>[] classesFromCache = cache.identityBy();
//        if (classesFromCache.length == 0 || classesFromCache.length == args.length)
//            return args;
//
//        Class<?>[] parameterTypes = method.getParameterTypes();
//        List<Object> resultArgs = new ArrayList<>();
//
//        for (int i = 0; i < parameterTypes.length; i++) {
//            for (int j = 0; j < classesFromCache.length; j++) {
//                if (parameterTypes[i].equals(classesFromCache[j])) {
//                    resultArgs.add(args[i]);
//                    classesFromCache[j] = null;
//                }
//            }
//        }
//
//        return resultArgs.toArray();
//    }

//    private Object checkItemsAmountToCache(Method method, Object value) {
//        if (method.getReturnType().equals(List.class)) {
//            List<Object> result = (List<Object>) value;
//            int toIndex = method.getAnnotation(Cache.class).listItemsAmountToCache();
//            System.out.println("LIST toIndex " + toIndex + " " + result.size());
//            if (toIndex < result.size())
//                result = new ArrayList<>(result.subList(0, toIndex));
//            return result;
//        } else
//            return value;
//    }

    private void serialize(Method method, Object[] args) throws Throwable {
        Object[] identityArguments = getIdentityArguments(method, args);
        String fileName = generateFileName(method, identityArguments, rootPath);
        Object result = invoke(method, args);
        result = checkItemsAmountToCache(method, result);
        System.out.println("result: " + result);
        SerializationUtils.serialize(new Result(result), fileName);

        if (method.getAnnotation(Cache.class).zip()) {
            System.out.println("creating ZIP...");
            SerializationUtils.zipFile(fileName);
            File serFile = new File(fileName);
            if (serFile.exists())
                serFile.delete();
        }
    }

    private Object deserialize(Method method, Object[] args) throws IOException, ClassNotFoundException {
        Object[] identityArguments = getIdentityArguments(method, args);
        Object result = null;
        String fileName = generateFileName(method, identityArguments, rootPath);

        if (!method.getAnnotation(Cache.class).zip()) {
            Result myResult = SerializationUtils.deserialize(fileName);
            result = myResult.getResult();
            System.out.println("Result: " + myResult.getResult());
        } else {
            String zipFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".zip";
            if (new File(zipFileName).exists()) {
                ZipFile zipFile = new ZipFile(zipFileName);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                if (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    List<Integer> buffer = new ArrayList<>();
                    int tmp = 0;

                    while (inputStream.available() > 0) {
                        buffer.add(inputStream.read());
                    }
                    byte[] bytes = new byte[buffer.size()];
                    for (int i = 0; i < buffer.size(); i++) {
                        Integer integer = buffer.get(i);
                        bytes[i] = (byte)integer.intValue();
                    }
                    Result myResult = SerializationUtils.deserialize(bytes);
                    result = myResult.getResult();
                }
            }
        }
        System.out.println("result: " + result);
        return result;
    }
}
