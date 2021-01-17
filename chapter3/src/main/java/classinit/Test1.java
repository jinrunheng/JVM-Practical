package classinit;

import classloader.MyClassLoader;

public class Test1 {
    public static void main(String[] args) throws ClassNotFoundException {
        MyChild child = new MyChild();
        // 程序输出中无法看出，但是只有调用接口的变量或方法时，接口才会被初始化
        // System.out.println(child.myInterface);

        // 调用ClassLoader装载一个类的时候，并不会初始化一个类，这不是对类的主动使用
        System.out.println("======start======");
        MyClassLoader myClassLoader = new MyClassLoader("my");
        Class cls = myClassLoader.loadClass("classinit.MyChild");
        System.out.println("======over======");
    }
}
