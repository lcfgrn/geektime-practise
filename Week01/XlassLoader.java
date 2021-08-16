package jvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

public class XlassLoader extends ClassLoader {
    public static void main(String[] args) throws Exception {
        // 类名和方法名
        String className = "Hello";
        String methodName = "hello";

        ClassLoader classLoader = new XlassLoader();

        Class<?> aClass = classLoader.loadClass(className);

//        for (Method m : aClass.getMethods()) {
//            System.out.println(m.getName());
//        }

        // 创建对象
        Object obj = aClass.getDeclaredConstructor().newInstance();

        // 调用方法
        Method method = aClass.getMethod(methodName);
        method.invoke(obj);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try (FileInputStream input = new FileInputStream("E:\\IdeaProjects\\geektime-java\\out\\production\\geektime-java\\resource\\Hello.xlass")) {
            int len = input.available();
            byte[] bytes = new byte[len];
            input.read(bytes);
            // 按字节转换xlass
            byte[] classBytes = decode(bytes);
            return defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    private byte[] decode(byte[] bytes) {
        int len = bytes.length;
        byte[] targetBytes = new byte[len];
        for (int i = 0; i < len; i++) {
            targetBytes[i] = (byte) (0b11111111 ^ bytes[i]);
        }
        return targetBytes;
    }


}
