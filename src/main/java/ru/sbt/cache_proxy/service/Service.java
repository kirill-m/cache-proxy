package ru.sbt.cache_proxy.service;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.annotations.CacheType;

import java.util.Date;
import java.util.List;

/**
 * Created by kirill on 19.08.16
 */
public interface Service {

    @Cache(cacheType = CacheType.FILE, fileNamePrefix = "data", zip = true, identityBy = {String.class, double.class})
    List<String> run(String item, double value, Date date);

    @Cache(cacheType = CacheType.IN_MEMORY, listItemsAmountToCache = 100_000)
    List<String> work(String item);
}
