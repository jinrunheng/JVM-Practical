package section3;

public class MyThread implements Runnable {
    private A a = null;
    private String name = "";

    public MyThread(A a, String name) {
        this.a = a;
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 0; i < 1000; i++) {
            a.aPlus();
        }
        System.out.println("thread " + name + "is over");
    }
}
