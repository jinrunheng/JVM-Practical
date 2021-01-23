## 第五章 字节码执行顺序

#### 5-1 栈帧和局部变量表

##### JVM字节码执行引擎概述

- JVM的字节码执行引擎，功能基本就是输入字节码文件，然后对字节码进行解析并处理，最后输出执行的结果
- 实现方式可能有通过解释器直接解释执行字节码，或者是通过即时编译器产生本地代码，也就是编译执行，当然也可能两者皆有
  - 解释执行：将编译好的字节码一行一行地翻译为机器码执行
  - 编译执行：以方法为单位，将字节码一次性翻译为机器码后执行

##### 栈帧概述

- 栈帧数用于支持JVM进行方法调用和方法执行的数据结构
- 栈帧随着方法调用而创建，随着方法结束而销毁
- 栈帧里面存储了方法的局部变量，操作数栈，动态连接，方法返回地址等信息

##### 栈帧概念结构

1. 局部变量表

   局部变量表数用来存放方法参数和方法内部定义的局部变量的存储空间

   - 以变量槽Slot为单位，目前一个Slot存放32位以内的数据类型

   - 对于64位的数据则占2个Slot

   - 对于实例方法，第0位Slot存放的是this，然后从1到n一次分配给参数列表

     示例程序

     ```java
     public class TestInstanceMethod {
         public int add(int a, int b) {
             int c = a + b;
             return c;
     
             // 局部变量表
             // Slot
             // 0 -- this
             // 1 -- a
             // 2 -- b
             // 3 -- c
         }
     
         public static void main(String[] args) {
     
         }
     }
     ```

     使用javap反编译：

     ```
     /Library/Java/JavaVirtualMachines/jdk-11.0.8.jdk/Contents/Home/bin/javap -verbose out.production.JVM-Practical.section1.execute.TestInstanceMethod 
     ```

     反编译的结果，我们直接来看add方法

     ```java
     public int add(int, int);
         descriptor: (II)I
         flags: (0x0001) ACC_PUBLIC
         Code:
           stack=2, locals=4, args_size=3
              0: iload_1
              1: iload_2
              2: iadd
              3: istore_3
              4: iload_3
              5: ireturn
           LineNumberTable:
             line 5: 0
             line 6: 4
           LocalVariableTable:
             Start  Length  Slot  Name   Signature
                 0       6     0  this   Lsection1/execute/TestInstanceMethod;
                 0       6     1     a   I
                 0       6     2     b   I
                 4       2     3     c   I
     ```

     我们可以看到`Slot0`指向了`this`,然后`slot1,2,3`依次存储了局部变量`a,b,c`

     我们再看下静态方法：

     ```java
     public class TestStaticMethod {
         public static int add(int a, int b) {
             int c = a + b;
             return c;
     
             // 局部变量表
             // Slot
             // 0 -- a
             // 1 -- b
             // 2 -- c
         }
     
         public static void main(String[] args) {
     
         }
     }
     ```

     使用javap反编译：

     ```
     /Library/Java/JavaVirtualMachines/jdk-11.0.8.jdk/Contents/Home/bin/javap -verbose out.production.JVM-Practical.section1.execute.TestStaticMethod 
     ```

     反编译的结果，我们还是直接来看静态的add方法

     ```java
     public static int add(int, int);
         descriptor: (II)I
         flags: (0x0009) ACC_PUBLIC, ACC_STATIC
         Code:
           stack=2, locals=3, args_size=2
              0: iload_0
              1: iload_1
              2: iadd
              3: istore_2
              4: iload_2
              5: ireturn
           LineNumberTable:
             line 5: 0
             line 6: 4
           LocalVariableTable:
             Start  Length  Slot  Name   Signature
                 0       6     0     a   I
                 0       6     1     b   I
                 4       2     2     c   I
     
     ```

     我们看到`Slot0`直接指向了方法的局部变量

   - 根据方法体内部定义的变量顺序和作用域来分配Slot

#### 5-2 案例：Slot是复用的

Slot是复用的，用来节省栈帧的空间，这种设计可能会影响到系统的垃圾收集行为

我们来看一个非常有趣的例子：

首先，我在VM options中设定了一些参数：

```
-XX:+UseConcMarkSweepGC -Xmx8m 
```

我们制定了堆最大内存为8MB

```java
public class TestInstanceMethod2 {
    
    public static void main(String[] args) {

        {
            byte[] bytes = new byte[2 * 1024 * 1024]; // 2MB
        }
        
        System.gc();

        System.out.println("totalMemory : " + Runtime.getRuntime().totalMemory());
        System.out.println("freeMemory : " + Runtime.getRuntime().freeMemory());
        System.out.println("maxMemory : " + Runtime.getRuntime().maxMemory());
    }
}
```

该程序运行的结果为：

```
totalMemory : 8126464
freeMemory : 4738600
maxMemory : 8126464
```

我们可以看到貌似`System.gc()`并没有将`bytes`数组占用的`2MB`内存回收掉

然后，我们改动程序：

```java
public class TestInstanceMethod2 {

    public static void main(String[] args) {

        {
            byte[] bytes = new byte[2 * 1024 * 1024]; // 2MB
        }
      
        int a = 5;
        System.gc();

        System.out.println("totalMemory : " + Runtime.getRuntime().totalMemory());
        System.out.println("freeMemory : " + Runtime.getRuntime().freeMemory());
        System.out.println("maxMemory : " + Runtime.getRuntime().maxMemory());
    }
}
```

看下程序的输出结果：

```
totalMemory : 8126464
freeMemory : 6840040
maxMemory : 8126464
```

我们惊讶地发现，freeMemory反而变大了，就说明`System.gc()`将`bytes`数组的`2MB`回收掉了！

这内部究竟发生了什么呢？

第一个程序：

```
Slot 
 0 ---> args
 1 ---> bytes ---> heap:2MB
```

`System.gc()`无法回收堆中`2MB`的内存，是因为局部变量`Slot1`指向着`bytes`;所以，GC认为这个`bytes`它还是有用的

第二个程序：

我们在代码块后面添加了一句话

```java
int a = 5;
```

因为`Slot`具有可以复用的特性，所以，在代码块执行完毕以后，我们又声明了一个变量`a`,代码块执行完毕以后，`Slot1`认为存储的`bytes`已经没有用了！所以，它会复用存储`a`这个变量！

GC也就认为`bytes`指向堆中的`2MB`内存是垃圾了，所以将其回收，所以，freeMemory反而会变大！

#### 5-3 案例：操作数栈

操作数栈是用来存放方法运行期间，各个指令操作的数据

我们来看一个基本的程序：

```java
public class TestOperandStack {
    public int add(int a, int b) {
        int c = a + b;
        return c;
    }

    public static void main(String[] args) {
        TestOperandStack test = new TestOperandStack();
        int res = test.add(1, 2);
        System.out.println("res = " + res);
    }
}
```

我们来看下使用`javap`命令反编译后，对于`add`方法的部分：

```java
  public int add(int, int);
    descriptor: (II)I
    flags: (0x0001) ACC_PUBLIC
    Code:
      stack=2, locals=4, args_size=3
         0: iload_1
         1: iload_2
         2: iadd
         3: istore_3
         4: iload_3
         5: ireturn
      LineNumberTable:
        line 5: 0
        line 6: 4
      LocalVariableTable:
        Start  Length  Slot  Name   Signature
            0       6     0  this   Lsection3/TestOperandStack;
            0       6     1     a   I
            0       6     2     b   I
            4       2     3     c   I
```

我们可以简单看下操作数栈发生了什么

```java
0: iload_1 // 首先将slot1的值加载到操作数栈
1: iload_2 // 然后将slot2的值加载到操作数栈
2: iadd    // 将操作数栈的值相加
3: istore_3// 将相加的值存储到slot3
4: iload_3 // 将slot3的值加载到操作数栈
5: ireturn // 将操作数栈的栈顶部值返回
```

关于操作数栈，有以下几点需要注意：

1. 操作数栈中元素的数据类型必须和字节码指令的顺序严格匹配
2. 虚拟机在实现栈帧的时候可能会做一些优化，让两个栈帧出现部分重叠区域，以存放公用的数据

##### 动态连接

动态连接是指每个栈帧持有一个指向运行时常量池中该栈帧所属方法的引用，以支持方法调用过程的动态连接

动态连接分为两种：

1. 静态解析：类加载的时候，符号引用就转化成直接引用
2. 动态连接：运行期间转换为直接引用

##### 方法返回地址

方法返回地址是方法执行后返回的地址，方法结束后，必须要返回方法调用的那个位置

##### 方法调用

方法调用就是确定具体调用哪一个方法，并不涉及方法内部的执行过程

1. 部分方法是直接在类加载的解析阶段，就确定了直接引用关系
2. 但是对于实例方法，也称虚方法，因为重载和多态，需要运行期动态委派

#### 5-4 静态分派和动态分派

静态分派：

所有依赖静态类型来定位方法执行版本的分派方式，比如重载方法

示例：

```java
public class TestStaticDispatch {
    public void t1(int a){
        System.out.println("t1(int) a = " + a);
    }

    public void t1(String a){
        System.out.println("t1(String) a = " + a);
    }

    public static void main(String[] args) {
        TestStaticDispatch t = new TestStaticDispatch();
        t.t1("s");
        t.t1(5);
    }
}
```

动态分派：

根据运行期的实际类型来定位方法执行版本的分派方式，比如覆盖方法

示例程序：

```java
public class TestDynamicDispatch extends TestStaticDispatch{
    @Override
    public void t1(String a) {
        System.out.println("TestDynamicDispatch t1(String) a = " + a);
    }

    public static void main(String[] args) {
        TestStaticDispatch t = new TestDynamicDispatch();
        t.t1("hello");
    }
}
```

程序输出的结果为：

```
TestDynamicDispatch t1(String) a = hello
```

本程序就是通过动态分派的方式，来看到底`t`指向的是哪一个实例，因为指向的是`TestDynamicDispatch`的实例，所以执行的就是`TestDynamicDispatch`的`t1`方法。

分派有分为：

- 单分派
- 多分派

单分派和多分派就是按照分派思考的维度，多余一个的就是多分派，只有一个多就称为单分派

如果说一个程序中，既有重载也有覆盖，那么很显然就是多分派，程序在这里就不再演示了，非常简单。

##### 如何执行方法中的字节码指令

JVM通过基于栈的字节码解释执行引擎来执行指令，JVM的指令集也是基于栈的