package ru.sbt.cache_proxy.main;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.domain.Result;
import ru.sbt.cache_proxy.serialization.SerializationUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by kirill on 21.08.16
 */
public class CacheProxyUtils {

    public static String generateFileName(Method method, Object[] args, String rootPath) {
        String prefix = method.getAnnotation(Cache.class).fileNamePrefix();
        if ("default".equals(prefix))
            prefix = method.getName();
        StringBuilder sb = new StringBuilder();
        sb.append(prefix)
                .append("_");

        for (Object arg : args) {
            sb.append(arg).append("_");
        }
        sb.replace(sb.lastIndexOf("_"), sb.lastIndexOf("_"), ".ser");

        String fileName = sb.toString();
        fileName = fileName.substring(0, fileName.lastIndexOf("_"));

        return rootPath + fileName;
    }

    public static Object[] getIdentityArguments(Method method, Object[] args) {
        Cache cache = method.getAnnotation(Cache.class);
        Class<?>[] classesFromCache = cache.identityBy();
        if (classesFromCache.length == 0 || classesFromCache.length == args.length)
            return args;

        Class<?>[] parameterTypes = method.getParameterTypes();
        List<Object> resultArgs = new ArrayList<>();

        for (int i = 0; i < parameterTypes.length; i++) {
            for (int j = 0; j < classesFromCache.length; j++) {
                if (parameterTypes[i].equals(classesFromCache[j])) {
                    resultArgs.add(args[i]);
                    classesFromCache[j] = null;
                }
            }
        }

        return resultArgs.toArray();
    }

    public static Object checkItemsAmountToCache(Method method, Object value) {
        if (method.getReturnType().equals(List.class)) {
            List<Object> result = (List<Object>) value;
            int toIndex = method.getAnnotation(Cache.class).listItemsAmountToCache();
            if (toIndex < result.size())
                result = new ArrayList<>(result.subList(0, toIndex));
            return result;
        } else
            return value;
    }

    public static void serialize(Method method, Object[] args, Object delegate, String rootPath) throws Throwable {
        Object[] identityArguments = getIdentityArguments(method, args);
        String fileName = generateFileName(method, identityArguments, rootPath);
        Object result = invoke(method, args, delegate);
        result = checkItemsAmountToCache(method, result);
        SerializationUtils.serialize(new Result(result), fileName);

        if (method.getAnnotation(Cache.class).zip()) {
            SerializationUtils.zipFile(fileName);
            File serFile = new File(fileName);
            if (serFile.exists())
                serFile.delete();
        }
    }

    public static Object invoke(Method method, Object[] args, Object delegate) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Impossible");
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public static Object deserialize(Method method, Object[] args, String rootPath) throws IOException, ClassNotFoundException {
        Object[] identityArguments = getIdentityArguments(method, args);
        Object result = null;
        String fileName = generateFileName(method, identityArguments, rootPath);

        if (!method.getAnnotation(Cache.class).zip()) {
            Result myResult = SerializationUtils.deserialize(fileName);
            result = myResult.getResult();
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
        return result;
    }
}
