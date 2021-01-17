package classinit;

public class Test3 {

    public static void main(String[] args) {
        MyTestClass myTestClass = MyTestClass.getInstance();
        System.out.println("myTestClass.a : " + myTestClass.getA());
        System.out.println("myTestClass.b : " + myTestClass.getB());
    }
}
