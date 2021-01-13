package classloader;

import java.sql.Driver;

public class ClassLoaderStudy {
    public static void main(String[] args) throws ClassNotFoundException {
        String str = "Hello";
        // 输出结果为null,因为BootstrapClassLoader不允许为外部调用
        System.out.println("str class loader : " + str.getClass().getClassLoader());

        Class driver = Class.forName("java.sql.Driver");
        // PlatformClassLoader
        System.out.println("driver class loader : " + driver.getClassLoader());
        // PlatformClassLoader的父级为BootstrapClassLoader
        System.out.println("driver parent class loader : " + driver.getClassLoader().getParent());

        ClassLoaderStudy classLoaderStudy = new ClassLoaderStudy();
        // AppClassLoader
        System.out.println("classLoaderStudy class loader : " + classLoaderStudy.getClass().getClassLoader());
        // AppClassLoader的父级为PlatformClassLoader
        System.out.println("classLoaderStudy parent class loader : " + classLoaderStudy.getClass().getClassLoader().getParent());
        // PlatformClassLoader的父级为BootstrapClassLoader
        System.out.println("classLoaderStudy parent.parent class loader : " + classLoaderStudy.getClass().getClassLoader().getParent().getParent());

        // AppClassLoader 除了加载classpath路径中的类库，还要加载应用级别的模块 例如：jshell
        Class jshell = Class.forName("jdk.jshell.JShell");
        System.out.println("jshell class loader : " + jshell.getClassLoader());
    }
}
