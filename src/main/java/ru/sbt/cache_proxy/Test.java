package ru.sbt.cache_proxy;

import ru.sbt.cache_proxy.main.CacheProxy;
import ru.sbt.cache_proxy.service.Service;
import ru.sbt.cache_proxy.service.ServiceImpl;

import java.util.Date;

/**
 * Created by kirill on 19.08.16
 */
public class Test {
    public static void main(String[] args) {
        CacheProxy proxy = new CacheProxy("path", new Date());
        Service service = proxy.cache(new ServiceImpl());
        service.work("lol");
    }
}
