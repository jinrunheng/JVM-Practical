## 第四章 内存分配

#### 4-1 JVM的简化架构和运行时数据区

JVM的简化架构示意图如下：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmtc2fsjh3j312u0ry78g.jpg" alt="image-20210119213828468" style="zoom:50%;" align="left"/>

##### 运行时数据区域

- PC寄存器(程序计数器)
- Java虚拟机栈
- Java堆
- 方法区
- 运行时常量池
- 本地方法栈
- ... ...

##### PC寄存器

PC寄存器(Program Counter)说明：

1. 每个线程拥有一个PC寄存器，是线程私有的，用来存储指向下一条指令的地址
2. 在创建线程的时候，创建相应的PC寄存器
3. 执行本地方法时，PC寄存器的值为`undefined`
4. PC寄存器是一块小的内存空间，是唯一一个在JVM规范中没有规定`OutOfMemoryError`的内存区域

##### Java栈

1. 栈由一系列帧(Frame)组成，它是线程私有的
2. 帧用来保存一个方法的局部变量，操作数栈(Java没有寄存器，所有参数传递使用操作数栈)，常量池指针，动态链接，方法返回值等
3. 每一次方法调用创建一个帧，并压栈，退出方法的时候，修改栈顶之后真就可以把栈帧中的内容销毁
4. 局部变量表存放了编译期可知的各种基本数据类型和引用类型，每个`slot`存放32位的数据，`long`,`double`占两个槽位
5. 栈的优点：存取速度比堆快，仅次于寄存器
6. 栈的缺点：存在栈中的数据大小，生存期是在编译期决定的，缺乏灵活性

##### Java堆

1. 用来存放应用系统创建的对象和数组，所有线程共享Java堆
2. GC主要就管理堆空间，对分代GC来说，堆也是分代的
3. 堆的优点：运行期动态分配内存大小，自动进行垃圾回收
4. 堆的缺点：效率相对较慢

##### 方法区

1. 方法区是线程共享的，通常用来保存装载类的结构信息
2. 通常和元空间关联在一起，但具体的跟JVM实现和版本有关
3. JVM规范把方法区描述为堆的一个逻辑部分，但它有一个别称`Non-heap`(非堆),应是为了与Java堆区分开

##### 运行时常量池

1. 是`Class`文件中每个类或接口的常量池表，在运行期间的表示形式，通常包括：类的版本，字段，方法，接口等信息
2. 在方法区中分配
3. 通常在加载类和接口到JVM，就创建相应的运行时常量池

##### 本地方法栈

1. 在JVM中用来支持`native`方法执行的栈就是本地方法栈

##### 栈，堆，方法区交互关系

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmtd97y1n4j31d00jydju.jpg" alt="image-20210119221929252" style="zoom: 33%;" align="left"/>

#### 4-2 Java堆内存模型和分配

##### Java堆内存概述

- Java堆用来存放应用系统创建的对象和数组，所有线程共享Java堆
- Java堆是在运行期动态分配内存大小，自动进行垃圾回收
- Java垃圾回收(GC)主要就是用来回收堆内存的，对分代GC来说，堆也是分代的

##### Java堆的结构

heap区分为：

- Eden Space（伊甸园）
- Survivor Space（幸存者区）
- Old Gen（老年代）

示例图如下：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmtdprhysfj317e0n0acu.jpg" alt="image-20210119223526103" style="zoom:50%;" align="left"/>

##### 关于Java堆

- 新生代用来放新分配的对象；新生代中经过垃圾回收，没有回收掉的对象，（从To Space）被复制到老年代
- 老年代存储对象比新生代存储对象的年龄大得多
- 老年代存储一些大对象
- 整个堆的大小 = 新生代 + 老年代
- 新生代 = Eden + 存活区(To Space和From Space只能用一块)
- 从前的持久代，用来存放Class，Method等元信息的区域，从JDK8开始去掉了，取而代之的是元空间(Meta Space)。元空间并不在虚拟机里面，而是直接使用本地内存

##### 对象的内存布局

- 对象在内存中存储的布局（以HotSpot虚拟机为例说明），分为：对象头，实例数据和对齐填充
- 对象头，包含两个部分：
  - Mark Word：存储对象自身的运行数据，如：HashCode，GC分代年龄，锁状态标志等
  - 类型指针：对象指向它的类元数据的指针
- 实例数据：真正存放对象实例数据的地方
- 对齐填充：这部分不一定存在，也没有什么特别含义，仅仅是占位符。因为HotSpot要求对象起始地址都是8字节的整数倍，如果不是就对齐

##### 对象的访问定位

- 对象的访问定位

  在JVM规范中只规定了reference类型是一个指向对象的引用，但没有规定这个引用具体如何去定位，访问堆中对象的具体位置

- 因此对象的访问方式取决于JVM的实现，目前主流的有：使用句柄 或 使用指针 两种方式

- 使用句柄：

  Java堆中会划分出一块内存来作为句柄池，reference中存储句柄的地址，句柄中存储对象的实例数据和类元数据的地址

  如下图：

  <img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmte8xlb5lj30wy0j8n4m.jpg" alt="image-20210119225353731" style="zoom:33%;" align="left"/>

- 使用指针：

  Java堆中会存放访问类元数据的地址，reference存储的就直接是对象的地址，如下图所示：

  <img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmteayznegj30yq0fqqgc.jpg" alt="image-20210119225550559" style="zoom:33%;" align="left"/>

- 通过句柄访问对象是一种间接引用(2次引用)的方式来进行访问堆内存的对象，它导致的缺点是运行的速度稍微慢一些；通过指针的方式则速度快一些，因为它少了一次指针定位的开销，所以HotSpot采用的就是指针的方式

#### 4-3 Trace跟踪和Java堆的参数配置

##### Trace跟踪参数

- 可以打印GC的简要信息：`-Xlog:gc`
- 打印GC的详细信息：`-Xlog:gc*`
- 指定GC log的位置，以及文件输出：`-Xlog:gc:garbage-collection.log`
- 每一次GC后，都打印堆信息：`-Xlog:gc+heap=debug`

##### GC日志格式

- ​	GC发生的时间，也就是JVM从启动以来经过的秒数
- 日志级别信息，和日志类型标记
- GC识别号
- GC类型和说明GC的原因
- 容量：GC前容量 -> GC后容量（该区域总容量）
- GC持续时间，单位秒。有的收集器会有更详细的描述，比如：user表示应哟哦那个程序消耗的时间，sys表示系统内核消耗的时间，real表示操作从开始到结束的时间

##### Java堆堆参数

- Xms：初始堆大小，默认物理内存的1/64

  示例程序：

  ```java
  public class Test1 {
      public static void main(String[] args) {
          System.out.println("totalMemory = " + Runtime.getRuntime().totalMemory()/1024/1024 + "MB");
          System.out.println("freeMemory = " + Runtime.getRuntime().freeMemory()/1024/1024 + "MB");
          System.out.println("maxMemory = " + Runtime.getRuntime().maxMemory()/1024/1024 + "MB");
      }
  }
  ```

  在未设置Xms参数之前程序的输出结果为：

  ```
  totalMemory = 128MB
  freeMemory = 125MB
  maxMemory = 2048MB
  ```

  然后我们在Idea下的`Run -> Edit Configurations -> VM options`下设置堆初始化内存大小为10M，即：`-Xms10m`

  接着重新运行程序：

  ```
  totalMemory = 10MB
  freeMemory = 7MB
  maxMemory = 2048MB
  ```

- Xmx：初始化最大堆的大小，默认物理内存的1/4

  我们还在Idea下的`Run -> Edit Configurations -> VM options`下设置最大堆的大小为10M，即：`-Xms10m -Xmx10m`

  程序运行结果为：

  ```
  totalMemory = 10MB
  freeMemory = 7MB
  maxMemory = 10MB
  ```

- 同常我们都会设置Xms和Xmx数值相等，这样做的好处是GC过后，JVM不必重新调整堆的大小，减少了系统每次分配内存的开销

#### 4-4 新生代配置和GC日志格式

刚刚我们介绍完了Xms和Xmx

##### Java堆的参数

- Xms：初始堆大小，默认是物理内存的1/64
- Xmx：最大堆大小，默认物理内存的1/4
- Xmn：新生代大小，默认整个堆的3/8；新生代主要存放新创建的对象
- -XX:+HeapDumpOnOutOfMemoryError:OOM时导出堆到文件
- -XX:+HeapDumpPath:导出OOM的路径

综合实践：

在`VM options`中输入：

```
-XX:+UseConcMarkSweepGC -XX:InitialHeapSize=9m -Xmx10m -Xmn3m -Xlog:gc+heap=debug
```

程序为：

```java
import java.util.ArrayList;
import java.util.List;

public class Test1 {
    private byte[] bs = new byte[1024 * 1024]; // 1MB

    public static void main(String[] args) {
        List<Test1> list = new ArrayList<>();
        int num = 0;
        try {
            while (true) {
                list.add(new Test1());
                num++;
            }
        } catch (Throwable err) {
            System.out.println("error, num = " + num);
            err.printStackTrace();
        }


        System.out.println("totalMemory = " + Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0 + "MB");
        System.out.println("freeMemory = " + Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0 + "MB");
        System.out.println("maxMemory = " + Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 + "MB");
    }
}

```

程序最后一次GC输出结果：

```
[0.150s][debug][gc,heap] GC(8) Heap before GC invocations=5 (full 3): par new generation   total 2816K, used 2052K [0x00000007ff600000, 0x00000007ff900000, 0x00000007ff900000)
[0.150s][debug][gc,heap] GC(8)   eden space 2560K,  80% used [0x00000007ff600000, 0x00000007ff8013c0, 0x00000007ff880000)
[0.150s][debug][gc,heap] GC(8)   from space 256K,   0% used [0x00000007ff880000, 0x00000007ff880000, 0x00000007ff8c0000)
[0.150s][debug][gc,heap] GC(8)   to   space 256K,   0% used [0x00000007ff8c0000, 0x00000007ff8c0000, 0x00000007ff900000)
[0.150s][debug][gc,heap] GC(8)  concurrent mark-sweep generation total 7168K, used 6914K [0x00000007ff900000, 0x0000000800000000, 0x0000000800000000)
[0.150s][debug][gc,heap] GC(8)  Metaspace       used 6092K, capacity 6159K, committed 6528K, reserved 1056768K
[0.150s][debug][gc,heap] GC(8)   class space    used 525K, capacity 538K, committed 640K, reserved 1048576K
[0.153s][info ][gc,heap] GC(8) ParNew: 2052K->2052K(2816K)
[0.153s][info ][gc,heap] GC(8) CMS: 6914K->6902K(7168K)
[0.153s][debug][gc,heap] GC(8) Heap after GC invocations=6 (full 4): par new generation   total 2816K, used 2052K [0x00000007ff600000, 0x00000007ff900000, 0x00000007ff900000)
[0.153s][debug][gc,heap] GC(8)   eden space 2560K,  80% used [0x00000007ff600000, 0x00000007ff801330, 0x00000007ff880000)
[0.153s][debug][gc,heap] GC(8)   from space 256K,   0% used [0x00000007ff880000, 0x00000007ff880000, 0x00000007ff8c0000)
[0.153s][debug][gc,heap] GC(8)   to   space 256K,   0% used [0x00000007ff8c0000, 0x00000007ff8c0000, 0x00000007ff900000)
[0.153s][debug][gc,heap] GC(8)  concurrent mark-sweep generation total 7168K, used 6902K [0x00000007ff900000, 0x0000000800000000, 0x0000000800000000)
[0.153s][debug][gc,heap] GC(8)  Metaspace       used 6092K, capacity 6159K, committed 6528K, reserved 1056768K
[0.153s][debug][gc,heap] GC(8)   class space    used 525K, capacity 538K, committed 640K, reserved 1048576K
[0.156s][info ][gc,heap] GC(9) Old: 6902K->6899K(7168K)
error, num = 8
totalMemory = 9.75MB
freeMemory = 0.29339599609375MB
maxMemory = 9.75MB
java.lang.OutOfMemoryError: Java heap space
	at section3.memory.Test1.<init>(Test1.java:9)
	at section3.memory.Test1.main(Test1.java:16)
```

#### 4-5 案例：使用MAT进行内存分析

Trace跟踪参数：

- 指定GC log文件的位置，以文件输出：`-Xlog:gc:日志名称`

例如：

```
-Xlog:gc:garbage-collection.log
```

#### 4-6 案例：堆，栈，元空间的参数配置

##### Java堆的参数

- `-XX:NewRatio`:老年代与新生代的比值，如果`xms=xmx`,且设置了`xmn`的情况下，该参数不用设置

在VM options中输入

```
-XX:+UseConcMarkSweepGC -Xmx10m -XX:NewRatio=1 -Xlog:gc+heap=debug
```

这里面指的是老年代与新生代的比值为1

程序输出最后的结果：

```
[0.152s][debug][gc,heap] GC(5) Heap after GC invocations=4 (full 4): par new generation   total 4608K, used 3090K [0x00000007ff600000, 0x00000007ffb00000, 0x00000007ffb00000)
[0.152s][debug][gc,heap] GC(5)   eden space 4096K,  75% used [0x00000007ff600000, 0x00000007ff904918, 0x00000007ffa00000)
[0.152s][debug][gc,heap] GC(5)   from space 512K,   0% used [0x00000007ffa00000, 0x00000007ffa00000, 0x00000007ffa80000)
[0.152s][debug][gc,heap] GC(5)   to   space 512K,   0% used [0x00000007ffa80000, 0x00000007ffa80000, 0x00000007ffb00000)
[0.152s][debug][gc,heap] GC(5)  concurrent mark-sweep generation total 5120K, used 4838K [0x00000007ffb00000, 0x0000000800000000, 0x0000000800000000)
[0.152s][debug][gc,heap] GC(5)  Metaspace       used 6097K, capacity 6159K, committed 6528K, reserved 1056768K
[0.152s][debug][gc,heap] GC(5)   class space    used 526K, capacity 538K, committed 640K, reserved 1048576K
```

我们可以看到新生代+`from`或是`to`的大小为`5120`

`cms`老年代的大小为`5120`

两者的关系为`1:1`

- `-XX:SurvivorRatio`:

  它定义了新生代中Eden区域和Survivor区域(From幸存区或To幸存区)的比例，默认为8

  也就是说，Eden占新生代的8/10，From幸存区和To幸存区各占新生代的`1/10`

可以参考计算公式：

```
Eden = (R*Y)/(R+1+1)
From = Y/(R+1+1)
To   = Y/(R+1+1)
```

其中：

R:SurvivorRatio比例

Y:新生代空间大小

案例：

VM options设置：

```
-XX:+UseConcMarkSweepGC -Xmx15m -XX:NewRatio=3 -Xlog:gc+heap=debug -XX:SurvivorRatio=8
```

程序输出最后的GC日志：

```
[0.183s][debug][gc,heap] GC(8) Heap after GC invocations=7 (full 4): par new generation   total 3712K, used 3074K [0x00000007ff000000, 0x00000007ff400000, 0x00000007ff400000)
[0.183s][debug][gc,heap] GC(8)   eden space 3328K,  92% used [0x00000007ff000000, 0x00000007ff300950, 0x00000007ff340000)
[0.183s][debug][gc,heap] GC(8)   from space 384K,   0% used [0x00000007ff340000, 0x00000007ff340000, 0x00000007ff3a0000)
[0.183s][debug][gc,heap] GC(8)   to   space 384K,   0% used [0x00000007ff3a0000, 0x00000007ff3a0000, 0x00000007ff400000)
[0.183s][debug][gc,heap] GC(8)  concurrent mark-sweep generation total 12288K, used 12027K [0x00000007ff400000, 0x0000000800000000, 0x0000000800000000)
[0.183s][debug][gc,heap] GC(8)  Metaspace       used 6129K, capacity 6191K, committed 6528K, reserved 1056768K
[0.183s][debug][gc,heap] GC(8)   class space    used 532K, capacity 570K, committed 640K, reserved 1048576K
```

我们可以看到Eden区大概占据了新生代的8/10

- `-XX:+HeapDumpOnOutOfMemoryError`:OOM时导出堆到文件
- `-XX:+HeapDumpPath`:导出OOM的路径
- `-XX:OnOutOfMemoryError`:在OOM时，执行一个脚本

##### Java栈的参数

- `-Xss`:通常只有几百K的大小，决定了函数调用的深度

示例程序：

```java
public class TestStack {
    private int num = 0;

    private int callMe(int a, int b) {
        num++;
        return callMe(a + num, b + num);
    }

    public static void main(String[] args) {
        TestStack testStack = new TestStack();
        try {
            testStack.callMe(1, 1);
        } catch (Throwable e) {
            System.out.println("call times = " + testStack.num);
        }
    }
}
```

该程序是一个递归调用的死循环，其中`num`变量作为计数器可以计数我们递归栈的深度，也就是递归调用了多少次才会`StackOverflow`

程序输出：

```
call times = 15706
java.lang.StackOverflowError
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	... ...
```

我们可以通过在VM options中设置参数`-Xss`，来改变调用栈的大小

VM options中设置：

```
-Xss2m
```

也就是说设置栈的大小为`2MB`

接下来执行程序，输出的结果为：

```
call times = 47089
java.lang.StackOverflowError
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	at section6.TestStack.callMe(TestStack.java:8)
	... ...
```

我们看到，递归调用执行的次数大大提升，说明我们的设置是有效的

##### 元空间的参数

- `-XX:MetaspaceSize`:初始空间大小
- `-XX:MaxMetaspaceSize`:最大空间，默认是没有限制的
- `-XX:MinMetaspaceFreeRatio`:在GC之后，最小的Metaspace剩余空间容量的百分比
- `-XX:MaxMetaspaceFreeRatio`:在GC之后，最大的Metaspace剩余空间容量的百分比

##### 章节小结

- 重点是Java堆内存的参数







