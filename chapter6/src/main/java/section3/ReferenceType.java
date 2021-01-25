package section3;

import java.lang.ref.*;
import java.util.ArrayList;
import java.util.List;

public class ReferenceType {

    private static ReferenceQueue<User> referenceQueue = new ReferenceQueue();

    private static void printQueue(String str){
        Reference< ? extends User> rf = referenceQueue.poll();
        if(rf != null){
            System.out.println("the gc Object rf : " + str + " " + rf.get());
        }
    }

    // 软引用
    private static void testSoftReference() throws InterruptedException {
        List<SoftReference<User>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 被垃圾回收之后才会放到 referenceQueue 当中
            SoftReference<User> softReference = new SoftReference<>(new User("soft" + i),referenceQueue);
            System.out.println("now the soft user : " + softReference.get());
            list.add(softReference);
        }
        // 触发垃圾回收
        System.gc();

        // 线程sleep 1s
        Thread.sleep(1000L);

        printQueue("soft");
    }

    // 弱引用
    private static void testWeakReference() throws InterruptedException {
        List<WeakReference<User>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 被垃圾回收之后才会放到 referenceQueue 当中
            WeakReference<User> weakReference = new WeakReference<>(new User("weak" + i),referenceQueue);
            System.out.println("now the weak user : " + weakReference.get());
            list.add(weakReference);
        }
        // 触发垃圾回收
        System.gc();

        // 线程sleep 1s
        Thread.sleep(1000L);

        printQueue("weak");
    }

    // 虚引用
    private static void testPhantomReference() throws InterruptedException {
        List<PhantomReference<User>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // 被垃圾回收之后才会放到 referenceQueue 当中
            PhantomReference<User> phantomReference = new PhantomReference<>(new User("phantom" + i),referenceQueue);
            System.out.println("now the phantom user : " + phantomReference.get());
            list.add(phantomReference);
        }
        // 触发垃圾回收
        System.gc();

        // 线程sleep 1s
        Thread.sleep(1000L);

        printQueue("phantom");
    }

    public static void main(String[] args) throws InterruptedException {
        // testSoftReference();
        // testWeakReference();
        testPhantomReference();
    }
}
