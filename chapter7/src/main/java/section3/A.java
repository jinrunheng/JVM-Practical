package section3;

public class A {
    private volatile int a;

    public void aPlus() {
        a++;
    }

    public int getA() {
        return a;
    }
}
