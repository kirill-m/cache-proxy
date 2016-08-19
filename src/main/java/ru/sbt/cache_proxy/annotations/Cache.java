package ru.sbt.cache_proxy.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by kirill on 19.08.16
 */

@Retention(RUNTIME)
@Target(METHOD)
public @interface Cache {

//    public enum CacheType {
//        FILE,
//        IN_MEMORY
//    }

    public CacheType cacheType() default CacheType.IN_MEMORY;

    String fileNamePrefix() default "default";

    boolean zip() default false;

    Class[] identityBy() default {String.class, double.class};

    double listItemsAmountToCache() default 10;
}
