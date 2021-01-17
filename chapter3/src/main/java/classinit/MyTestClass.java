package classinit;

public class MyTestClass {
    private static MyTestClass myTestClass = new MyTestClass();

    private static int a = 0;
    private static int b;

    private MyTestClass() {
        a++;
        b++;
    }

    public static MyTestClass getInstance() {
        return myTestClass;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }
}
