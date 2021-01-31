## 第八章 性能监控与故障处理工具

#### 8-1 命令行工具

命令行工具：

- *jps*

  *jps(JVM Process Status Tool)*：主要用来输出 *JVM* 中运行的进程状态信息，语法格式如下：

  ```
  jps [options][hostid]
  ```

  *hostid* 字符串的语法与 *URI* 的语法基本一致：

  ```
  [protocol:][[//]hostname][:port][/servername]
  ```

  如果不指定*hostid*，默认当前主机或服务器

- *jinfo*

  打印给定进程或核心文件或远程调试服务器的配置信息。语法格式：

  ```
  jinfo [option] pid #指定进程号(pid)的进程
  ```

- *jstack*

  *jstack* 主要用来查看某个 *Java* 进程内的线程堆栈信息。语法格式如下：

  ```
  jstack [option] pid
  ```

- *jmap*

  *jmap* 用来查看堆内存使用状况，语法格式如下：

  ```
  jmap [option] pid
  ```

- *jstat*

  *JVM* 统计监测工具，查看各个区内存和 *GC* 的情况。语法格式如下：

  ```
  jstat [generalOption | outputOptions vmid [interval[s | ms][count]]]
  ```

- *jstatd*

  虚拟机的 *jstat* 守护进程，主要用于监控 *JVM* 的创建与终止，并提供一个接口，以允许远程监视共工具附加到在本地系统上运行的 *JVM*

- *jcmd*

  *JVM* 诊断命令工具，将诊断命令请求发送到正在运行的 *Java* 虚拟机，比如可以用来导出堆，查看 *Java* 进程，导出线程信息，执行 *GC* 等

图形化工具：

- *jconsole*
- *jmc*
- *visualvm*

两种连接方式：

- *JMX*
- *jstatd*

##### JVM监控工具的作用

- 对 *jvm* 运行期间对内部情况进行监控，比如：对 *jvm* 参数，*CPU* ，内存，堆等信息的查看
- 辅助进行性能调优
- 辅助解决应用运行时的一些问题，比如：*OutOfMemoryError*，内存泄漏，线程死锁，锁争用，*Java* 进程消耗 *CPU* 过高等等

#### 8-2 jconsole

*jconsole* 是一个用于监视 *Java* 虚拟机的符合 *JMX* 的图形工具。它可以监视本地和远程 *JVM*，还可以监视和管理应用程序

在命令行界面，直接运行 *jconsole*, 弹出的 *jconsole* 界面如图所示：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn6qewjwnij312e0u0wji.jpg" alt="image-20210131114700108" style="zoom:33%;" align="left"/>

#### 8-3 jmc

*jmc (JDK Mission Control)* ，*Java*任务控制（*JMC*）客户端包括用于监视和管理 *Java* 应用程序的工具，而不会引入通常与这些类型的工具相关联的性能开销

下载地址：

https://www.oracle.com/java/technologies/javase/products-jmc7-downloads.html

*jmc* 图形界面：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn6r4uihqaj31820u046w.jpg" alt="image-20210131121206192" style="zoom:33%;" align="left"/>



#### 8-4 VisualVM

*VisualVM* 是一个图形工具，它提供有关在 *Java* 虚拟机中运行的基于 *Java* 技术的应用程序的详细信息。

*Java VisualVM* 提供内存和 *CPU* 分析，堆转储分析，内存泄漏监测，访问 *MBean* 和垃圾回收

*VisualVM* 的下载地址：http://visualvm.github.io/download.html

#### 8-5 远程连接

两种远程连接方式：

- *JMX* 连接可以查看：系统信息，*CPU* 使用情况，线程多少，手动执行垃圾回收等比较偏于系统级层面的信息
- *jstatd* 连接可以提供：*JVM* 内存分布详细信息，垃圾回收分布图，线程详细信息，甚至可以看到某个对象使用内存的大小

##### 远程连接 Tomcat

配置 *JMX* 的支持，需要在 *tomcat* 的 *catalina.sh* 里面添加一些设置，样例如下：

```
CATALINA_OPTS="-Xms800m -XX:SurvivorRatio=8 -XX:+HeapDumpOnOutOfMemoryError -Dcom.sun.management.jmxremote=true -
Djava.rmi.server.hostname=192.168.1.105 -
Dcom.sun.management.jmxremote.port=6666 -
Dcom.sun.management.jmxremote.ssl=false -
Dcom.sun.managementote.ssl=false -
Dcom.sun,management.jmxremote.authenticate=false"
```

配置 *jstatd* ：

1. 自定义一个 *statd.policy* 文件，添加：

   ```
   grant codebase "jrt:/jdk.jstatd" {
   	permission java.security.AllPermission;
   };
   
   grant codebase "jrt:/jdk.internal.jvmstat" {
   	permission java.security.AllPermission;
   };
   ```

2. 然后在 *JDK_HOME/bin* 下面运行 *jstatd*，示例如：

   ```
   ./jstatd -J-Djava.rmi.server.hostname=192.168.1.102 -J-Djava.security.policy=java.policy -p 1099 
   ```

#### 8-6 监控实战1

##### 监控与故障处理实战

- 内存泄漏分析，线程查看，热点方法查看，垃圾回收查看
- 线程死锁

##### 内存泄漏分析

```java
package section7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MemoryTest {

    public static void main(String[] args) {

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<A> list = new ArrayList<>();
        for(int i = 0; i < 10000; i++){
            list.add(new A());

            if(i % 20 == 0){
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.gc();
        System.out.println("over");
        // 阻塞
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class A {
    private byte[] bs = new byte[10 * 1024];
}
```

通过 *jmc* 或 *VisualVM* 可以获取到 *CPU* 运行情况，内存变化曲线，线程分析等数据，分析内存泄漏。

#### 8-7 监控实战2

##### 线程死锁

示例代码：

```java
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
```

程序运行后，会引发线程死锁

从 *VisualVM* 工具的 *Threads* 分析栏中出现了 *Deadlock detected*，即：发现了线程死锁 

并且我们可以从分析中，找到死锁的原因，接着就可以将相互等待锁的问题发现并解决掉。



