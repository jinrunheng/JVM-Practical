package classinit;

public class MyTestClass2 {

    private static int a = 0;
    private static int b;

    private MyTestClass2(){
        a++;
        b++;
    }

    public int getA(){
        return a;
    }
    public int getB(){
        return b;
    }

    private static final MyTestClass2 myTestClass2 = new MyTestClass2();

    public static MyTestClass2 getInstance(){
        return myTestClass2;
    }
}
