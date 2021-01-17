package classinit;

public class Test4 {
    public static void main(String[] args) {
        MyTestClass2 myTestClass2 = MyTestClass2.getInstance();
        System.out.println("myTestClass2.a : " + myTestClass2.getA());
        System.out.println("myTestClass2.b : " + myTestClass2.getB());
    }
}
