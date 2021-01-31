package section8;

public class ThreadLockTest {
    public static void main(String[] args) {
        AModel am = new AModel("mys1", "mys2");
        for (int i = 0; i < 10; i++) {
            Thread t1 = new Thread(new MyThread1(am));
            t1.setName("MyThread1");
            t1.start();

            Thread t2 = new Thread(new MyThread2(am));
            t2.setName("MyThread2");
            t2.start();
        }
    }
}

class AModel {
    public String s1;
    public String s2;

    public AModel(String s1, String s2) {
        this.s1 = s1;
        this.s2 = s2;
    }
}

class MyThread1 implements Runnable {

    private AModel aModel = null;

    public MyThread1(AModel aModel) {
        this.aModel = aModel;
    }

    @Override
    public void run() {
        synchronized (aModel.s1) {
            System.out.println("now in myThread1, has aModel.s1");

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("now myThread1 begin get aModel.s2");

            synchronized (aModel.s2) {
                System.out.println("Thread id = " + Thread.currentThread().getId() +
                        ", s1 = " + aModel.s1 +
                        ", s2 = " + aModel.s2);
            }
        }
    }
}

class MyThread2 implements Runnable {

    private AModel aModel = null;

    public MyThread2(AModel aModel) {
        this.aModel = aModel;
    }

    @Override
    public void run() {
        synchronized (aModel.s2) {
            System.out.println("now in myThread2, has aModel.s2");

            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("now myThread2 begin get aModel.s1");

            synchronized (aModel.s1) {
                System.out.println("Thread id = " + Thread.currentThread().getId() +
                        ", s1 = " + aModel.s1 +
                        ", s2 = " + aModel.s2);
            }
        }
    }
}
