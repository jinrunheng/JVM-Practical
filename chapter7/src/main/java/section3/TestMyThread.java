package section3;

public class TestMyThread {
    public static void main(String[] args) throws InterruptedException {
        A a = new A();
        Thread t1 = new Thread(new MyThread(a,"t1"));
        Thread t2 = new Thread(new MyThread(a,"t2"));
        t1.start();
        t2.start();
        // 使用 join 以后，main主线程会等t1和t2都跑完以后才运行 a.getA()
        t1.join();
        t2.join();
        System.out.println("A.a = " + a.getA());
    }
}
