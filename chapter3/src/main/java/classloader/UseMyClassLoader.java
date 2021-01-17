package classloader;

public class UseMyClassLoader {
    public static void main(String[] args) throws ClassNotFoundException {
        MyClassLoader myClassLoader = new MyClassLoader("myClassLoader1");
        Class cls1 = myClassLoader.loadClass("classloader.MyClass");
        System.out.println("cls1 class loader : " + cls1.getClassLoader());
        System.out.println("cls1 parent class loader : " + cls1.getClassLoader().getParent());
    }
}
