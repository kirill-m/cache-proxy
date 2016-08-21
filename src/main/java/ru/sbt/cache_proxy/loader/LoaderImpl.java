package ru.sbt.cache_proxy.loader;

/**
 * Created by kirill on 19.08.16
 *
 * The class simulates slow operation.
 */
public class LoaderImpl implements Loader {
    @Override
    public String load(int pageNumber, String url) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return pageNumber + " " + url;
    }

    @Override
    public String load(int pageNumber) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Loaded: page" + pageNumber;
    }
}
