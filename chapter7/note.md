## 第七章 高效并发

#### 7-1 Java内存模型和内存间的交互操作

##### Java内存模型

*JCP* 定义了一种 *Java* 内存模型，以前是在 *JVM* 规范中，后来独立出来成为 *JSR-133* （ *Java* 内存模型和线程规范修订）

内存模型：

在特定的操作协议下，对特定的内存或高速缓存进行读写访问的过程抽象

*Java* 内存模型主要关注 *JVM* 中把变量值存储到内存和内存中取出变量值这样的底层细节

*Java*内存模型示意图：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn4y7rtomwj30oe0iq40l.jpg" alt="image-20210129224553425" style="zoom: 33%;" align="left"/>

*Java* 内存模型有以下几点要求：

1. 所有变量（共享的）都存储在主内存中，每个线程都有自己的工作内存；工作内存中保存该线程使用到的变量的主内存副本拷贝
2. 线程对变量的所有操作（读，写）都应该在工作内存中完成
3. 不同线程不能相互访问工作内存，交互数据要通过主内存

##### 内存间的交互操作

*Java* 内存模型规定了一些操作来实现内存交互，*JVM* 会保证它们是原子的

1. *lock*

   锁定，把变量标识为线程独占，作用于主内存变量

2. *unlock*

   解锁，把锁定的变量释放，别的线程才能使用，作用于主内存变量

3. *read*

   读取，把变量值从主内存读取到工作内存

4. *load*

   载入，把 *read* 读取到的值放入工作内存的变量副本中

5. *use*

   使用，把工作内存中的一个变量的值传递给执行引擎

6. *assign*

   赋值，把从执行引擎接收到的值赋给工作内存里面的变量

7. *store*

   存储，把工作内存中的一个变量的值传递到主内存中

8. *write*

   写入，把 *store* 进来的数据存放到主内存的变量中

#### 7-2 内存间的交互操作的规则

1. 不允许 *read* 和 *load*，*store* 和 *write* 操作之一单独出现，以上两个操作必须按顺序执行，但不保证连续执行；也就是说， *read* 和 *load* 之间、*store* 和 *write* 之间是可以插入其他指令的。
2. 不允许一个线程丢弃它最近的 *assign* 操作，即变量在工作内存中改变了之后必须把该变化同步回主内存。
3. 不允许一个线程无原因地（没有发生过任何 *assign* 操作）把数据从线程的工作内存同步回主内存中
4. 一个新的变量只能从主内存中 “诞生”，不允许在工作内存中直接使用一个未被初始化的变量，也就是对一个变量实施 *use* 和 *store* 操作之前，必须先执行过了 *assign* 和 *load* 操作。
5. 一个变量在同一个时刻只允许一条线程对其执行 *lock* 操作，但 *lock* 操作可以被同一条线程重复执行多次，多次执行 *lock* 后，只有执行相同次数的 *unlock* 操作，变量才会被解锁。
6. 如果一个变量执行 *lock* 操作，将会清空工作内存中此变量的值，在执行引擎使用这个变量前，需要重新执行 *load* 或 *assign* 操作初始化变量的值。
7. 如果一个变量没有被 *lock* 操作锁定，则不允许对它执行 *unlock* 操作，也不能 *unlock* 一个被其他线程锁定的变量。
8. 对于一个变量进行 *unlock* 操作之前，必须先把此变量同步回主内存（执行 *store* 和 *write* 操作） 

#### 7-3 volatile特性

##### 多线程中的可见性

可见性：就是一个线程修改了变量，其他线程可以知道

保证可见性的常见方法：*volatile*，*synchronized*，*final（一旦初始化完成，其他线程就不可见）*

##### volatile

*volatile* 基本上是 *JVM* 提供的最轻量级的同步机制，用 *volatile* 修饰的变量，对所有的线程可见，即对 *volatile* 变量所做的写操作能立即反映到其他线程中。

**用 *volatile* 修饰的变量，在多线程环境下仍然是不安全的！**

示例：

*A*

```java
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
```

*MyThread*

```java
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
```

*TestMyThread*

```java
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
```

程序运行结果：

```
thread t2is over
thread t1is over
A.a = 1796
```

我们期待的运行结果为：**A.a = 2000**，所以通过示例程序也可以看出 *volatile* 是不安全的

如果想要程序输出正确的结果，我们还需要在 *aPlus* 方法前加上 *synchronized* 关键字

***volatile* 修饰的变量，是禁止指令重排优化的**

使用使用 *volatile* 的场景：

- 运算结果不依赖变量的当前值
- 或者能确保只有一个线程修改变量的值

#### 7-4 指令重排原理和规则

指令重排指的是 *JVM* 为了优化，在条件允许的情况下，对指令进行一定的重新排列，直接运行当前能够立即执行的后续指令，避开获取下一条指令所需数据造成的等待。

指令重排只考虑线程内串行语义，不考虑多线程间的语义。

不是所有的指令都能重排比如：

- 写后读 `a = 1; b = a;`，写一个变量之后，再读这个变量
- 写后写`a = 1; a = 2;`，读一个变量之后，再写这个变量
- 读后写`a = b; b = 1;`，读一个变量之后，再写这个变量

##### 指令重排的基本规则

- 程序顺序原则：一个线程内保证语义的串行性
- *volatile* 规则：*volatile* 变量的鞋，先发生于读
- 锁规则：解锁（*unlock*）必然发生在随后的加锁（*lock*）之前
- 传递性：*A* 先于 *B*，*B* 先于 *C* ，那么 *A* 必然先于 *C*
- 线程的 *start* 方法先于它的每一个动作
- 线程的所有操作优先于线程的终结（*Thread.join()*）
- 线程中断（*interrupt()*）先于被中断线程的代码
- 对象的构造函数执行结束先于 *finlize()* 方法

#### 7-5 代码示例：指令重排的各种情况分析

```java
package section5;

public class TestInstructionRearrangement {
    private static int x = 0;
    private static int y = 0;
    private static int a = 0;
    private static int b = 0;

    public static void main(String[] args) throws InterruptedException {
        // case1 ： t1 先运行完，然后 t2 再运行  结果为 a = 1,b = 2,x = 0,y = 1
        // case2 ： t2 先运行完，然后 t1 再运行  结果为 a = 1,b = 2,x = 2,y = 0
        // case3 ： t1,t2交叉运行；t1先运行部分，然后t2运行完，t1再运行剩余部分 结果为 a = 1,b = 2,x = 2,y = 1
        // case4 ： t1,t2交叉运行；t2先运行部分，然后t1运行完，t2再运行剩余部分 结果为 a = 1,b = 2,x = 2,y = 1
        // case5 ： t1,t2交叉运行；t1先部分运行，接着t2也部分运行，然后t1运行完，最后t2运行完 结果为 a = 1,b = 2,x = 2,y = 1
        // case6 ： t1,t2交叉运行；t2先部分运行，接着t1也部分运行，然后t2运行完，最后t1运行完 结果为 a = 1,b = 2,x = 2,y = 1

        // 重排的情况下，可能会出现 ： x = 0, y = 0 的情况
        for(int i = 0; i < 10000; i++){
             a = 0;
             b = 0;
             x = 0;
             y = 0;

            Thread t1 = new Thread(() -> {
                try {
                    if(System.currentTimeMillis() % 8 == 0){
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a = 1;
                x = b;
            });

            Thread t2 = new Thread(() -> {
                b = 2;
                y = a;
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();
            
            System.out.println("i = " + i +  " x = " + x + " y = " + y);
        }
    }
}
```

在我的主机上，试了很多次，并没有出现 `x = 0,y = 0`这种情况，但是它是会有概率发生的。

##### 多线程中的有序性

1. 在本线程内，操作都是有序的
2. 在线程外观察，操作都是无序的，因为存在指令重排或主内存同步延时

#### 7-6 线程安全处理

- 不可变（*final*）是线程安全的

- 互斥同步（阻塞同步）：*synchronized*，*java.util.concurrent.ReentrantLock*。

  目前这两个方法性能已经差不多了，建议优先选用 *synchronized*，*ReentrantLock* 增加了如下特性：

  - 等待可中断：当持有锁的线程长时间不释放锁，正在等待的线程可以选择放弃等待。
  - 公平锁：多个线程等待同一个锁时，需严格按照申请锁的时间顺序来获得锁。
  - 锁绑定多个条件：一个 *ReentrantLock* 对象可以绑定多个 *condition* 对象，而 *synchronized* 是针对一个条件的，如果要多个，就得有多个锁。

- 非阻塞同步：是一种基于冲突检查的乐观锁定策略，通常是先操作，如果没有冲突，操作就成功了，有冲突再采取其他方式进行补偿处理。

- 无同步方案：其实就是在多线程中，方法并不涉及共享数据，自然也就无需同步了。

*ReentrantLock* 程序演示：

*A*

```java
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
```

*MyThread*

```java
package section6;

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
        System.out.println("thread " + name + " is over");
    }
}
```

*MyTest*

```java
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
```

程序输出结果：

```
thread t1 is over
thread t2 is over
A.a = 2000
```

可以看到，和我们在 *aPlus()* 方法修饰符中加入 *synchronized* 的效果是一样的，同样可以保证程序的线程安全。

#### 7-7 锁优化

##### 自旋锁与自适应自旋

自旋：

如果线程可以很快获得锁，那么可以不在 *OS* 层挂起线程，而是让线程做几个忙循环，这就是自旋。

自适应自旋：

自旋的时间不再固定，而是由前一次在同一个锁上的自旋时间和锁的拥有者状态来决定。

如果锁被占用时间很短，自旋成功，那么能节省线程挂起，以及切换时间，从而提升系统性能。

如果锁被占用时间很长，自旋失败，会白白耗费处理器资源，降低系统性能。

##### 锁消除

- 在编译代码的时候，检测到根本不存在共享数据竞争，自然也就无需同步加锁了；通过`-XX:+EliminateLocks` 来开启
- 同时要使用`-XX:+DeEscapeAnalysis` 开启逃逸分析，所谓逃逸分析：
  - 如果一个方法中定义的一个对象，可能被外部方法引用，称为方法逃逸
  - 如果对象可能被其他外部线程访问，称为线程逃逸，比如赋值给类变量或者可以在其他线程中访问的实例变量

##### 锁粗化

- 通常我们都要求同步块要小，但一系列连续的操作导致对一个对象反复的加锁和解锁，这回导致不必要的性能损耗。这种情况建议把锁同步的范围加大到整个操作序列。

##### 轻量级锁

- 轻量级是相对于传统锁机制而言，本意是没有多线程竞争的情况下，减少传统锁机制使用 *OS* 实现互斥所产生的性能损耗
- 其实现原理很简单，就是类似乐观锁的方式
- 如果轻量级锁失败，表示存在竞争，升级为重量级锁，导致性能下降

##### 偏向锁

- 偏向锁是在无竞争情况下，直接把整个同步消除了，连乐观锁都不用，从而提高性能；所谓的偏向，就是偏心，即锁会偏向于当前已经占有锁的线程
- 只要没有竞争，获得偏向锁的线程，在将来进入同步块，也不需要做同步
- 当有其他线程请求相同的锁时，偏向模式结束
- 如果程序中大多数锁总是被多个线程访问的时候，也就是竞争比较激烈，偏向锁反而会降低性能
- 使用`-XX:+UseBiasedLocking`来禁用偏向锁，默认开启

##### JVM中获取锁的步骤

- 会先尝试偏向锁；然后尝试轻量级锁
- 如果拿不到轻量级锁，就会尝试自旋锁
- 最后尝试普通锁，使用 *OS* 互斥量在操作系统层挂起

##### 同步代码的基本规则

- 尽量减少锁持有的时间
- 尽量减小锁的粒度





