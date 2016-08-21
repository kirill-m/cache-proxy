package ru.sbt.cache_proxy.main;

import ru.sbt.cache_proxy.annotations.Cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
            System.out.println("LIST toIndex " + toIndex + " " + result.size());
            if (toIndex < result.size())
                result = new ArrayList<>(result.subList(0, toIndex));
            return result;
        } else
            return value;
    }

//    private void serialize(Method method, Object[] args) throws Throwable {
//        Object[] identityArguments = getIdentityArguments(method, args);
//        String fileName = generateFileName(method, identityArguments);
//        Object result = invoke(method, args);
//        result = checkItemsAmountToCache(method, result);
//        System.out.println("result: " + result);
//        SerializationUtils.serialize(new Result(result), fileName);
//
//        if (method.getAnnotation(Cache.class).zip()) {
//            System.out.println("creating ZIP...");
//            SerializationUtils.zipFile(fileName);
//            File serFile = new File(fileName);
//            if (serFile.exists())
//                serFile.delete();
//        }
//    }
}
