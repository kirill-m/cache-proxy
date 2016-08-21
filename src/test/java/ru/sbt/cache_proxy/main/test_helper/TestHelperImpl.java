package ru.sbt.cache_proxy.main.test_helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by kirill on 21.08.16
 */
public class TestHelperImpl implements TestHelper {
    @Override
    public List<String> example(String item, double value, Date date) {
        return new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3"}));
    }

    @Override
    public List<String> example2(String item) {
        return new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3"}));
    }

    @Override
    public List<String> exampleWithDefaultAnnotation(String item, double number) {
        return null;
    }

    @Override
    public void methodWithoutAnnotation() {

    }
}
