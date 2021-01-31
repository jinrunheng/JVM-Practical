package section6;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class A {
    private int a;
    // 默认为不公平锁
    private final Lock lock = new ReentrantLock();

    public void aPlus(){
        lock.lock();

        a++;

        lock.unlock();
    }

    public int getA(){
        return a;
    }
}
