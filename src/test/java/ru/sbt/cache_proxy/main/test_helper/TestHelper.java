package ru.sbt.cache_proxy.main.test_helper;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.annotations.CacheType;

import java.util.Date;
import java.util.List;

/**
 * Created by kirill on 21.08.16
 */
public interface TestHelper {

    @Cache(cacheType = CacheType.FILE, fileNamePrefix = "data", zip = true, identityBy = {String.class, double.class})
    List<String> example(String item, double value, Date date);

    @Cache(cacheType = CacheType.IN_MEMORY, listItemsAmountToCache = 4)
    List<String> example2(String item);

    @Cache
    List<String> exampleWithDefaultAnnotation(String item, double number);

    void methodWithoutAnnotation();
}
