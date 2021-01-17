package classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyClassLoader extends ClassLoader {

    private String myName = "";

    public MyClassLoader(String myName) {
        this.myName = myName;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = this.loadClassData(name);
        return this.defineClass(name, data, 0, data.length);
    }

    private byte[] loadClassData(String className) {
        byte[] data = null;
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        className = className.replace(".", "/");
        try (out) {
            in = new FileInputStream(new File("classes/" + className + ".class"));
            int b = 0;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            data = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
