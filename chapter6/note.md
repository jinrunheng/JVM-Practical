## 第六章 垃圾回收

#### 6-1 垃圾回收基础和根搜索算法

垃圾回收概述

- 什么是垃圾

  简单说就是内存中已经不再被使用到的内存空间就是垃圾

- 如何判断垃圾

  - 引用计数法

    给对象添加一个引用计数器，有访问就加1，引用失效就减1

    引用计数法的优缺点：

    - 优点：实现简单，效率高
    - 缺点：不能解决对象之间循环引用的问题

  - 根搜索算法

    从根(GC Roots)节点向下搜索对象节点，搜索走过的路径称为引用链，当一个对象到根之间没有连通的话，则该对象不可用

    根搜索算法示意图：

    <img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn09kxzd6qj312a0pc78p.jpg" alt="image-20210125213122056" style="zoom:33%;" align="left"/>

    可以作为GC Roots的对象包括：

    - 虚拟机栈(栈帧局部变量)中引用的对象
    - 方法区类静态属性引用的对象
    - 方法区中常量(final)引用的对象
    - 本地方法栈中JNI引用的对象

    **HotSpot使用了一组叫做OopMap的数据结构达到准确式GC的目的**

    在OopMap的协助下，JVM可以很快做完GC Roots枚举。

    但是JVM并没有为每一条指令生成一个OopMap。

    记录OopMap的这些“特定位置”被称为安全点(**safe point**)，即当前线程执行到安全点后才允许暂停进行GC

    如果一段代码中，对象引用关系不会发生变化，这个区域中任何地方开始GC都是安全的，那么这个区域称为安全区域(**safe region**)

#### 6-2 引用分类

##### 强引用

类似于

```java
Object a = new A();
```

这种说强引用，不会被回收

##### 软引用

软引用是指还有用但并不是必须的对象。也就是说如果进行了垃圾回收，内存判定还不够，才会回收软引用的对象。用`SoftReference`来实现软引用

##### 弱引用

非必须对象，比软引用还要弱，垃圾回收时会回收掉。用`WeakReference`来实现弱引用

##### 虚引用

虚引用也称为幽灵引用或幻影引用，是最弱的引用。垃圾回收时会被回收掉。用`PhantomReference`来实现虚引用

#### 6-3 案例：各种引用的实现

GC回收软引用案例

*User*

```java
public class User {
    
    private String name;

    public User(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "User name : " + name;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("now finalize user : " + name);
    }
}
```

其中`finalize`方法是：垃圾回收器准备释放内存的时候，会先调用`finalize`方法

*ReferenceType*

```java
package section3;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
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
            // 如果被垃圾回收，则会放到 referenceQueue 当中
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

    public static void main(String[] args) throws InterruptedException {
        testSoftReference();
    }
}
```

修改VM options

```
-XX:+UseConcMarkSweepGC -Xmx2m
```

程序运行结果：

```
now the soft user : User name : soft0
now the soft user : User name : soft1
now the soft user : User name : soft2
now the soft user : User name : soft3
now the soft user : User name : soft4
now the soft user : User name : soft5
now the soft user : User name : soft6
now the soft user : User name : soft7
now the soft user : User name : soft8
now the soft user : User name : soft9
```

这也就意味着，没有发生GC回收

我们修改User的代码：

*User*

```java
public class User {
    private byte[] bytes = new byte[10 * 1024]; // 新增代码，大小为10KB
    private String name;

    public User(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "User name : " + name;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("now finalize user : " + name);
    }
}
```

其他代码不动，接着运行程序，输出结果：

```
now the soft user : User name : soft0
now the soft user : User name : soft1
now the soft user : User name : soft2
now the soft user : User name : soft3
now the soft user : User name : soft4
now the soft user : User name : soft5
now the soft user : User name : soft6
now the soft user : User name : soft7
now the soft user : User name : soft8
now the soft user : User name : soft9
now finalize user : soft9
now finalize user : soft8
now finalize user : soft7
now finalize user : soft6
now finalize user : soft5
now finalize user : soft4
now finalize user : soft3
now finalize user : soft2
now finalize user : soft1
now finalize user : soft0
the gc Object rf : soft null
```

我们可以看到，因为我们初始化内存大小为2MB，在创建软引用对象后，内存空间不足，GC触发回收机制

接着，我们可以将其他的引用回收都用代码测试下：

*testWeakReference*

```java
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
```

即便不在`User`中添加`10KB`的内存，弱引用的对象也会被GC回收掉

程序输出结果：

```
now the weak user : User name : weak0
now the weak user : User name : weak1
now the weak user : User name : weak2
now the weak user : User name : weak3
now the weak user : User name : weak4
now the weak user : User name : weak5
now the weak user : User name : weak6
now the weak user : User name : weak7
now the weak user : User name : weak8
now the weak user : User name : weak9
now finalize user : weak7
now finalize user : weak9
now finalize user : weak8
now finalize user : weak6
now finalize user : weak5
now finalize user : weak4
now finalize user : weak3
now finalize user : weak2
now finalize user : weak1
now finalize user : weak0
the gc Object rf : weak null

Process finished with exit code 0
```

*testPhantomReference*

```java
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
```

单独测试虚引用，程序输出结果：

```
now the phantom user : null
now finalize user : phantom0
now the phantom user : null
now the phantom user : null
now the phantom user : null
now the phantom user : null
now the phantom user : null
now the phantom user : null
now the phantom user : null
now the phantom user : null
now the phantom user : null
now finalize user : phantom8
now finalize user : phantom9
now finalize user : phantom7
now finalize user : phantom6
now finalize user : phantom5
now finalize user : phantom4
now finalize user : phantom3
now finalize user : phantom2
now finalize user : phantom1
the gc Object rf : phantom null
```

#### 6-4 垃圾回收基础【跨代引用,记忆集,写屏障,判断垃圾的步骤,STW】

##### 跨代引用

跨代引用是指一个代中的对象引用另一个代中的对象，比如新生代中的一个对象引用了老年代中的对象

当然，跨代引用相对于同代引用来说只是极少数的，这就是**跨代引用假说**

也可以得出一个隐含推论：存在互相引用关系的两个对象，是应该倾向于同时生存或同时消亡的

跨代引用带来的问题就是：降低了回收效率。

为了解决这个问题，引入了一种新的数据结构：记忆集

##### 记忆集

记忆集：**Remembered Set**

它是一种用于记录从非收集区域指向收集区域的指针集合的抽象数据结构

简单来说，就是记忆集是一种单独使用的抽象数据结构，来记录跨代引用

记忆集有记忆精度，实现记忆精度有以下几种方式：

- 字长精度：每个记录精确到一个机器字长，该字包含跨代指针
- 对象精度：每个记录精确到一个对象，该对象里还有字段含有跨代指针
- 卡精度（最常使用的方式）：每个记录精确到一块内存区域，该区域内有对象含有跨代指针

卡表(**Card Table**)是记忆集的一种具体实现定义了记忆集的记录精度以及与堆内存的映射关系等

卡表的每个元素都对应着其标识的内存区域中一块特定大小的内存块，这个内存块称为卡页(**Card Page**)

##### 写屏障

写屏障可以看成是JVM对“引用类型字段赋值”这个动作的AOP

通过写屏障来实现当对象状态改变后，维护卡表状态，这就是写屏障的功能

##### 判断垃圾的步骤

1. 根搜索算法判断不可用

2. 看是否有必要执行`finalize`方法

   当对象第一次被回收的时候，调用`finalize`方法

   如果对象没有覆盖`finalize`方法，或`finalize`已经被虚拟机调用过，这属于没有必要执行`finalize`

   在Java中不建议调用`finalize`，不过在`finalize`方法可以实现“对象自救”

   我们来看一个实例程序：

   ```java
   public class HelpSelf {
       private static HelpSelf hs = null;
   
       @Override
       protected void finalize() throws Throwable {
           super.finalize();
           System.out.println("now in finalize");
           // 对象自救
           hs = this;
       }
   
       public static void main(String[] args) throws InterruptedException {
           hs = new HelpSelf();
   
           // 第一次
           hs = null;
           System.gc();
           // System.gc()不一定会垃圾回收，添加线程sleep方法，增加GC的可行性
           Thread.sleep(1000L);
           System.out.println("first hs : " + hs);
   
           // 第二次
           hs = null;
           System.gc();
           Thread.sleep(1000L);
           System.out.println("first hs : " + hs);
       }
   }
   ```

   该程序输出结果为：

   ```
   now in finalize
   first hs : section4.HelpSelf@2d6e8792
   first hs : null
   ```

   出现这个结果的原因是：

   第一次GC时，程序会调用`finalize`方法，在`finalize`方法将`hs`重新指向了`this`，所以它没有被回收！

   而第二次GC时，程序就不会调用`finalize`了，因为`finalize`只能被调用一次！所以，`hs`指向了`null`，堆内存空间里面的对象被回收

3. 上面两个步骤走完之后，对象仍然没有人使用，那就属于垃圾

##### GC类型

- MinorGC/YoungGC：发生在新生代的收集动作
- MajorGC/OldGC：发生在老年代的GC，目前只有CMS收集器会有单独收集老年代的行为
- MixedGC：收集整个新生代以及部分老年代，目前只有G1收集器会有这种行为
- FullGC：收集整个Java堆和方法区的GC

##### Stop-The-World

STW是Java中一种全局暂停的现象，多半由于GC引起。所谓全局停顿，就是所有Java代码停止运行，native代码可以执行，但是不能和JVM进行交互

这种现象是我们应当极力避免的

STW的危害是长时间服务停止，没有响应；对于HA系统，可能引起主备切换，严重危害生产环境

##### 垃圾收集类型

- 串行收集：GC单线程内存回收，会暂停所有的用户线程，如：`Serial`
- 并行收集：多个GC线程并发工作，此时用户线程是暂停的，如：`Parallel`
- 并发收集：用户线程和GC线程同时执行(不一定是并行，可能交替执行)，不需要停顿用户线程，如：`CMS`

##### 判断类无用的条件

- JVM中该类的所有实例都已经被回收
- 加载该类的ClassLoader已经被回收
- 没有任何地方引用该类的Class对象
- 无法在任何地方通过反射访问这个类

#### 6-5 垃圾回收算法

- 标记清除法
- 复制算法
- 标记整理法

##### 标记清除法

标记清除法(Mark-Sweep)；该算法分成标记和清除两个阶段，现标记出要回收的对象，然后统一回收这些对象

标记清除法示意图：

**回收前(标记阶段)：**

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn0egtwxs9j30vc0ag0vo.jpg" alt="image-20210126002026737" style="zoom:50%;" align="left"/>

**回收后(清除阶段)：**

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn0eixsasrj30uy09wgon.jpg" alt="image-20210126002228964" style="zoom:50%;" align="left"/>

标记清除法：

- 优点：简单

- 缺点：

  - 效率不高，标记和清除分离，而且标记和清除的效率都不高

  - 标记清除后会产生大量不连续的内存碎片，从而导致在分配大对象时触发GC

    举个例子，拿上面回收后的示意图来看，我们在清除对象后，有很多不连续的未使用空间，如果有一个大对象，需要占三格的空间大小，但是我们目前的情况并不存在这样的连续空间可以存放这个大对象，这时候就会强迫GC，导致效率低

##### 复制算法

复制算法(Copying)就是把内存分成两块完全相同的区域，每次使用其中一块，当一块使用完了，就把这块上还存活的对象拷贝到另外一块，然后把这块清除掉

这就是新生代存活区里面的`From`和`To`应用的算法

复制算法示意图：

**回收前（左侧可用）：**

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn0exaqroaj30v60d477u.jpg" alt="image-20210126003458034" style="zoom:50%;" align="left"/>

**回收后（右侧可用）：**

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn0f11094aj30v20da0vu.jpg" alt="image-20210126003951962" style="zoom:50%;" align="left"/>

复制算法：

- 优点：实现简单，运行高效，不用考虑内存碎片的问题
- 缺点：内存有些浪费
- JVM实际实现中，是将内存分为一块较大的Eden区和两块较小的Survivor空间，每次使用Eden和一块Survivor，回收时，将存活的对象复制到另一块Suvivor
- HotSpot默认的Eden和Survivor比例是`8 : 1`,也就是说每次能用`90%`的新生代空间
- 如果Survivor空间不够，就要依赖老年代进行分配担保，把放不下的对象直接进入老年代

那么什么是分配担保呢？

##### 分配担保

分配担保是当新生代进行垃圾回收后，新生代的存活区放置不下，那么就需要把这些对象放置到老年代去的策略，也就是老年代为新生代的GC做空间分配担保，步骤如下：

1. 在发生MinorGC之前，JVM会检查老年代最大可用的连续空间，是否大于新生代所有对象的总空间，如果大于，可以确保MinorGC是安全的
2. 如果小于，那么JVM会检查是否设置了允许担保失败，如果允许，则继续检查老年代最大可用的连续空间，是否大于历次晋升到老年代对象的平均大小
3. 如果大于，则尝试一次MinorGC
4. 如果不大于，则改做一次Full GC

##### 标记整理法

标记整理算法(Mark-Compact)，由于复制算法在存活对象比较多的时候，效率较低，且有浪费空间，因此老年代一般不会选用复制算法，老年代多选用标记整理算法

标记整理法的标记过程和标记清除法的标记是一样的，但是后续不是直接清除可回收对象，而是让所有存活对象都向一端移动，然后直接清除边界以外的内存

标记整理法示意图：

**回收前（标记可回收对象）：**

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn0flzy1kej30uc0980vj.jpg" alt="image-20210126010009174" style="zoom:50%;" align="left"/>

**回收后（整理）：**

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn0fon80e2j30uc09iwha.jpg" alt="image-20210126010234114" style="zoom:50%;" align="left"/>

标记整理算法：

- 优点：解决了内存碎片问题
- 缺点：整理阶段，由于移动了存活对象的位置，所以需要去更新引用

#### 6-6 垃圾收集器基础和串行收集器

##### 垃圾收集器概述

前面讨论的垃圾收集算法只是内存回收的方法，垃圾收集器就来具体实现这些算法并实现内存回收

因此，不同厂商，不同版本的虚拟机实现垃圾收集器的差别是很大的，HotSpot包含的收集器如下图所示：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn1es2zrh7j317q0lggp7.jpg" alt="image-20210126211644028" style="zoom:50%;" align="left"/>

##### 串行收集器

串行收集器(Serial/Serial Old)，是一个单线程的收集器，在垃圾收集时，会发生**Stop-the-world**

串行收集器运行示意图：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn1f7yh76zj31b80kudka.jpg" alt="image-20210126213212246" style="zoom:50%;" align="left"/>

串行收集器：

- 优缺点：

  简单，对于单cpu，由于没有多线程的交互开销，可能更高效，是默认的Client模式下的新生代收集器

- 使用：

  `-XX:+UseSerialGC`来开启，会使用：`Serial + Serial Old`的收集器组合

- 对于串行收集器，新生代使用复制算法，老年代使用标记-整理算法

#### 6-7 并行收集器和Parallel Scavenge收集器

##### 并行收集器:ParNew

并行收集器使用多线程进行垃圾回收，在垃圾收集时，会**Stop-the-World**

ParNew 收集器运行示意图：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn1g7pmnelj318o0j278x.jpg" alt="image-20210126220627379" style="zoom:50%;" align="left"/>

并行收集器：

- 在并发能力好的CPU环境里，它停顿的时间要比串行收集器短；但对于单CPU或并发能力较差的CPU，由于多线程的交互开销，可能比串行收集器更差
- 是Server模式下首选的新生代收集器，且能和CMS收集器配合使用
- 已经不再使用`-XX:+UseParNewGC`来单独开启
- `-XX:ParallelGCThreads`:可以指定并行线程数，最好是与CPU数量一致
- 只在新生代使用，为复制算法

##### 新生代Parallel Scavenge收集器

新生代Parallel Scavenge收集器/Parallel Old 收集器：是一个应用于新生代的，使用复制算法的并行收集器

它本身和ParNew很类似，但是它更关注吞吐量，能最高效率的利用CPU，非常适合运行后台应用

新生代Parallel Scavenge 收集器运行示意图：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn1g5qji9dj319m0jq43v.jpg" alt="image-20210126220429430" style="zoom:50%;" align="left"/>

新生代Parallel Scavenge收集器:

- 使用`-XX:+UseParallelGC`来开启
- 使用`-XX:+UseParallelOldGC`来开启老年代使用`Parallel Old`收集器，使用`Parallel Scavenge + Parallel Old`的收集器组合
- `-XX:MaxGCPauseMillis`：设置GC的最大停顿时间