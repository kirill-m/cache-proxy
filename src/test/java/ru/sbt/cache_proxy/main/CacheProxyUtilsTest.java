package ru.sbt.cache_proxy.main;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import ru.sbt.cache_proxy.main.test_helper.TestHelper;
import ru.sbt.cache_proxy.main.test_helper.TestHelperImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static ru.sbt.cache_proxy.main.CacheProxyUtils.*;

/**
 * Created by kirill on 21.08.16
 */
@RunWith(MockitoJUnitRunner.class)
public class CacheProxyUtilsTest {
    List<String> largeList = new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7"}));
    List<String> smallList = new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3"}));

    @Test
    public void testGenerateFileNameWithPrefix() throws Exception {
        Method method = TestHelper.class.getMethod("example", String.class, double.class, Date.class);
        Object[] args = {"str", 12d};
        String fileName = generateFileName(method, args, "/path/");

        Assert.assertEquals("/path/data_str_12.0.ser", fileName);
    }

    @Test
    public void testGenerateFileNameWithoutPrefix() throws NoSuchMethodException {
        Method method = TestHelper.class.getMethod("example2", String.class);
        Object[] args = {"str"};
        String fileName = generateFileName(method, args, "/path/");

        Assert.assertEquals("/path/example2_str.ser", fileName);
    }

    @Test
    public void testGenerateArgsWithoutIdentityBy() throws NoSuchMethodException {
        Method method = TestHelper.class.getMethod("exampleWithDefaultAnnotation", String.class, double.class);
        Object[] args = {"str", 12d};
        Assert.assertEquals(2, getIdentityArguments(method, args).length);
    }

    @Test
    public void testGenerateArgsWithIdentityBy() throws NoSuchMethodException {
        Method method = TestHelper.class.getMethod("example", String.class, double.class, Date.class);
        Object[] args = {"str", 12d, new Date()};
        Assert.assertEquals(2, getIdentityArguments(method, args).length);
    }

    @Test
    public void testItemsAmountWithLessLimit() throws NoSuchMethodException {
        Method method = TestHelper.class.getMethod("example2", String.class);
        Object[] args = {"str"};
        List o = (List)checkItemsAmountToCache(method, largeList);
        Assert.assertEquals(4, o.size());
    }

    @Test
    public void testItemsAmountWithLargerLimit() throws NoSuchMethodException {
        Method method = TestHelper.class.getMethod("example2", String.class);
        Object[] args = {"str"};
        List<String> o = (List<String>)checkItemsAmountToCache(method, smallList);
        Assert.assertEquals(3, o.size());
    }

    @Test
    public void testSerializationSerFileExists() throws Throwable {
        Method method = TestHelper.class.getMethod("example2", String.class);
        Object[] args = {"str"};
        serialize(method,args, new TestHelperImpl(), "src/test/resources/");
        File file = new File("src/test/resources/example2_str.ser");
        Assert.assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void testSerializationZipFileExists() throws Throwable {
        Method method = TestHelper.class.getMethod("example", String.class, double.class, Date.class);
        Object[] args = {"str", 12d, new Date()};
        serialize(method,args, new TestHelperImpl(), "src/test/resources/");
        File file = new File("src/test/resources/data_str_12.0.zip");
        Assert.assertTrue(file.exists());
        file.delete();
    }

    @Test
    public void testDeserializationFromSerFile() throws NoSuchMethodException, IOException, ClassNotFoundException {
        Method method = TestHelper.class.getMethod("example2", String.class);
        Object[] args = {"str"};
        List<String> o = (List<String>) deserialize(method, args, "src/test/resources/files_to_deserialize/");
        Assert.assertEquals(3, o.size());
        Assert.assertArrayEquals(new String[]{"1", "2", "3"}, o.toArray());
    }

    @Test
    public void testDeserializationFromZipFile() throws NoSuchMethodException, IOException, ClassNotFoundException {
        Method method = TestHelper.class.getMethod("example", String.class, double.class, Date.class);
        Object[] args = {"str", 12d, new Date()};
        List<String> o = (List<String>) deserialize(method, args, "src/test/resources/files_to_deserialize/");
        Assert.assertEquals(3, o.size());
        Assert.assertArrayEquals(new String[]{"1", "2", "3"}, o.toArray());
    }
}