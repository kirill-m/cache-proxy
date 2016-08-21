package ru.sbt.cache_proxy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by kirill on 19.08.16
 *
 * * The class simulates slow operation.
 */
public class ServiceImpl implements Service {
    @Override
    public List<String> run(String item, double value, Date date) {
        System.out.println("Running with value = " + value);
        List<String> list = new ArrayList<>();
        list.add("one");
        list.add("two");
        list.add("three");
        list.add("four");
        list.add("five");

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    @Override
    public List<String> work(String item) {
        List<String> list = new ArrayList<>();
        list.add(item + " one");
        list.add(item + "two");
        list.add(item + "three");
        list.add(item + "four");
        list.add(item + "five");
        return list; //run(item, 0, new Date());
    }
}
