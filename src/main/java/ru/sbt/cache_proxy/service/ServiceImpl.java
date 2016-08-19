package ru.sbt.cache_proxy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kirill on 19.08.16
 */
public class ServiceImpl implements Service {
    @Override
    public List<String> run(String item, double value, Date date) {
        System.out.println("Running with value = " + value);
        List<String> list = new ArrayList<>();

        for (int i = 12; i < 1_000_000_000; i++) {
            list.add(item);
            value += i / (i - 1);
        }

        return list;
    }

    @Override
    public List<String> work(String item) {
        System.out.println("Working with item = " + item);
        return  new LinkedList<>(); //run(item, 0, new Date());
    }
}
