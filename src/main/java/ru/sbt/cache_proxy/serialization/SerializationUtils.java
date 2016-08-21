package ru.sbt.cache_proxy.serialization;

/**
 * Created by kirill on 20.08.16
 */

import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.asList;

public class SerializationUtils {


//    public static void serialize(Serializable o) throws IOException {
//        serialize(o, FILE);
//    }

//    public static <T> T deserialize(String file) throws IOException, ClassNotFoundException {
//        return deserialize(file);
//    }

    public static byte[] serializeToByte(Serializable o) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream stream = new ObjectOutputStream(
                bytes)) {
            stream.writeObject(o);
        }
        return bytes.toByteArray();
    }

    public static void serialize(Serializable o, String file) throws IOException {
        try (ObjectOutputStream stream = new ObjectOutputStream(
                new FileOutputStream(file))) {
            stream.writeObject(o);
        }
    }

    public static <T> T deserialize(String file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream stream = new ObjectInputStream(
                new FileInputStream(file))) {
            return (T) stream.readObject();
        }
    }

    public static <T> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        try (ObjectInputStream stream = new ObjectInputStream(
                new ByteArrayInputStream(bytes))) {
            return (T) stream.readObject();
        }
    }

    public static void zipFile(String file) {
        String zipFileName = file.substring(0, file.lastIndexOf(".")) + ".zip";
        try (FileOutputStream fos = new FileOutputStream(zipFileName);
             ZipOutputStream zos = new ZipOutputStream(fos);
             FileInputStream fis = new FileInputStream(new File(file))) {
            String fileName = file.substring(file.lastIndexOf(System.getProperty("file.separator")) + 1);
            System.out.println(fileName);
            ZipEntry e = new ZipEntry(fileName);
            zos.putNextEntry(e);

            int tmp = 0;
            while ((tmp = fis.read()) >= 0) {
                zos.write(tmp);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("No such file" + zipFileName + "found", e);
        } catch (IOException e) {
            throw new RuntimeException("Exception happened on autoclosable resource ZipOutputStream out", e);
        }
    }

    public void deleteFile(String file) {

    }

    public static Object key(Method method, Object[] args) {
        List<Object> key = new ArrayList<>();
        key.add(method);
        key.addAll(asList(args));

        return key;
    }
}
