package section6;

public class MyTest {
    public static void main(String[] args) throws InterruptedException {
        A a = new A();

        Thread t1 = new Thread(new MyThread(a,"t1"));
        Thread t2 = new Thread(new MyThread(a,"t2"));

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("A.a = " + a.getA());
    }
}
