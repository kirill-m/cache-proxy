package ru.sbt.cache_proxy.loader;

import ru.sbt.cache_proxy.annotations.Cache;

/**
 * Created by kirill on 19.08.16
 */
public interface Loader {
    @Cache
    String load(int pageNumber, String url);

    String load(int pageNumber);
}
