package ru.sbt.cache_proxy;

import ru.sbt.cache_proxy.loader.Loader;
import ru.sbt.cache_proxy.loader.LoaderImpl;
import ru.sbt.cache_proxy.main.CacheProxy;
import ru.sbt.cache_proxy.service.Service;
import ru.sbt.cache_proxy.service.ServiceImpl;

import java.util.Date;

/**
 * Created by kirill on 19.08.16
 */
public class Test {
    public static void main(String[] args) {
        CacheProxy proxy = new CacheProxy("/Users/kirill/Desktop/dz9/");
        Service service = proxy.cache(new ServiceImpl());
        service.work("lol");

        Loader loader = proxy.cache(new LoaderImpl());
        System.out.println(loader.load(1, "1"));
        System.out.println(loader.load(2, "1"));
        System.out.println(loader.load(2, "1"));
        System.out.println(loader.load(2, "1"));
        System.out.println(loader.load(2, "1"));
        System.out.println(loader.load(2, "1"));
        System.out.println(loader.load(2, "2"));
        System.out.println(loader.load(2, "2"));
        System.out.println(loader.load(1, "1"));
        System.out.println(loader.load(1, "1"));
        System.out.println(loader.load(1, "1"));
        System.out.println("__________________");

        service.run("1", 12, new Date(111111111));
        service.run("1", 12, new Date(111111122));

    }
}
