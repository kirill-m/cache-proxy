package ru.sbt.cache_proxy.domain;

import java.io.Serializable;

/**
 * Created by kirill on 20.08.16
 */
public class Result implements Serializable {
    private final Object result;

    public Result(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
