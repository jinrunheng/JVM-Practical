# JVM-Practical
JVM实战

| JVM实战                                                      | 源码与笔记                                                   |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| **第1章 课程导学与准备工作**                                 |                                                              |
| 第1章学习笔记                                                | [无笔记]                                                     |
| **第2章 认识JVM规范**                                        |                                                              |
| 第2章学习笔记                                                |                                                              |
| 2-1 从三种认知角度重识JVM                                    |                                                              |
| 2-2 JVM规范作用及其核心                                      |                                                              |
| 2-3 理解JVM规范中的虚拟机结构                                |                                                              |
| 2-4 如何学习JVM规范中的指令集                                |                                                              |
| 2-5 Class字节码解析：理解ClassFile结构                       |                                                              |
| 2-6 阅读Class字节码：常量池                                  |                                                              |
| 2-7 阅读Class字节码：类定义和属性                            |                                                              |
| 2-8 阅读Class字节码：方法和方法调用                          |                                                              |
| 2-9 ASM开发：编程模型和核心API                               |                                                              |
| 2-10 ASM开发：ClassVisitor开发                               |                                                              |
| 2-11 ASM开发：MethodVisitor开发                              |                                                              |
| 2-12 ASM开发：实现模拟AOP功能                                |                                                              |
| **第3章 类加载、连接和初始化**                               |                                                              |
| 第3章学习笔记                                                | [笔记](https://github.com/jinrunheng/JVM-Practical/blob/main/chapter3/note.md) |
| 3-1 类加载和类加载器                                         | [无代码]                                                     |
| 3-2 案例：类加载器使用                                       | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter3/src/main/java/classloader) |
| 3-3 双亲委派模型                                             | [无代码]                                                     |
| 3-4 案例：自定义你ClassLoader                                | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter3/src/main/java/classloader) |
| 3-5 双亲委派模型说明和代码示例                               | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter3/src/main/java/classloader) |
| 3-6 类连接和初始化                                           | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter3/src/main/java/classinit) |
| 3-7 案例：类的主动初始化                                     | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter3/src/main/java/classinit) |
| 3-8 案例：类的初始化机制和顺序                               | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter3/src/main/java/section8) |
| **第四章 内存分配**                                          |                                                              |
| 第四章学习笔记                                               | [笔记](https://github.com/jinrunheng/JVM-Practical/blob/main/chapter4/note.md) |
| 4-1 JVM的简化架构和运行时数据区                              | [无代码]                                                     |
| 4-2 Java堆内存模型和分配                                     | [无代码]                                                     |
| 4-3 案例：Trace跟踪和Java堆堆参数配置                        | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter4/src/main/java/section3/memory) |
| 4-4 案例：新生代配置和GC日志格式                             | [无代码]                                                     |
| 4-5 案例：使用MAT进行内存分析                                | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter4/src/main/java/section5) |
| 4-6 案例：堆，栈，元空间的参数配置                           | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter4/src/main/java/section6) |
| **第五章 字节码执行引擎**                                    |                                                              |
| 第五章学习笔记                                               | [笔记](https://github.com/jinrunheng/JVM-Practical/blob/main/chapter5/note.md) |
| 5-1 栈帧和局部变量表                                         | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter5/src/main/java/section1/execute) |
| 5-2 案例：slot是复用的                                       | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter5/src/main/java/section2) |
| 5-3 案例：操作数栈                                           | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter5/src/main/java/section3) |
| 5-4 静态分派和动态分派                                       | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter5/src/main/java/section4) |
| **第六章 垃圾回收**                                          |                                                              |
| 第六章学习笔记                                               | [笔记](https://github.com/jinrunheng/JVM-Practical/blob/main/chapter6/note.md) |
| 6-1 垃圾回收基础和根搜索算法                                 | [无代码]                                                     |
| 6-2 引用分类                                                 | [无代码]                                                     |
| 6-3 案例：各种引用的实现                                     | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter6/src/main/java/section3) |
| 6-4 垃圾回收基础【跨代引用,记忆集,写屏障,判断垃圾的步骤,STW】 | [code](https://github.com/jinrunheng/JVM-Practical/tree/main/chapter6/src/main/java/section4) |
| 6-5 垃圾回收算法                                             | [无代码]                                                     |
| 6-6 垃圾收集器基础和串行收集器                               | [无代码]                                                     |
| 6-7 并行收集器和Parallel Scavenge收集器                      | [无代码]                                                     |
| 6-8 CMS收集器                                                | [无代码]                                                     |
| 6-9 G1收集器                                                 | [无代码]                                                     |
| 6-10 ZGC收集器,GC性能指标和JVM内存配置原则                   | [无代码]                                                     |
| **第七章 高效并发**                                          |                                                              |
| 第七章学习笔记                                               |                                                              |
| 7-1 Java内存模型和内存见的交互操作                           |                                                              |
| 7-2 内存间的交互操作的规则                                   |                                                              |
| 7-3 volatile特性                                             |                                                              |
| 7-4 指令重排原理和规则                                       |                                                              |
| 7-5 代码示例：指令重排的各种情况分析                         |                                                              |
| 7-8 线程安全处理                                             |                                                              |
| 7-9 锁优化                                                   |                                                              |







