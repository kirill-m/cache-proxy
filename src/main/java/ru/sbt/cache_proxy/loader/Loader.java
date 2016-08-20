package ru.sbt.cache_proxy.loader;

import ru.sbt.cache_proxy.annotations.Cache;
import ru.sbt.cache_proxy.annotations.CacheType;

/**
 * Created by kirill on 19.08.16
 */
public interface Loader {
    @Cache(cacheType = CacheType.FILE)
    String load(int pageNumber, String url);

    String load(int pageNumber);
}
