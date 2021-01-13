## 第三章 类加载、连接和初始化

#### 3-1 类加载和类加载器

##### 类加载到JVM至卸载出内存的生命周期

类从被加载到JVM开始，到卸载出内存，整个生命周期如图所示：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmmdmh64ycj30jq0kcdi2.jpg" alt="image-20210113211308028" style="zoom:33%;" align="left"/>

```
加载 -> 连接(验证 -> 准备 -> 解析) -> 初始化 ->使用 -> 卸载
```



各个阶段的主要功能为：

- 加载：查找并加载类文件的二进制数据
- 连接：将已经读入内存的类的二进制数据合并到JVM运行时环境中去，包含如下几个步骤：
  - 验证：确保被加载类的正确性
  - 准备：为类的**静态变量**分配内存
  - 解析：把常量池中的符号引用转换成直接引用
- 初始化：为类的静态变量赋初始值

##### 类加载要完成的功能

1. 通过类的全限定类名来获取该类的二进制字节流
2. 把二进制字节流转化为方法区的运行时数据结构
3. 在堆上创建一个`java.lang.Class`对象，用来封装类在方法区内的数据结构，并向外提供了访问方法区内数据结构的接口

##### 加载类的方式

1. 最常见的方式：本地文件系统中加载，从`jar`等归档文件中加载
2. 动态的方式：将`java`源文件动态编译成`class`
3. 其它方式：网络下载，从专有数据库中加载等等

##### 类加载器

Java虚拟机自带的加载器包括如下几种：(JDK9开始)

- 启动类加载器(BootstrapClassLoader)
- 平台类加载器(PlatformClassLoader)
- 应用程序类加载器(AppClassLoader)

JDK8虚拟机自带的加载器：

- BootstrapClassLoader
- ExtensionClassLoader
- AppClassLoader

为什么从JDK9开始要将ExtensionClassLoader替换为PlatformClassLoader呢？

主要有两点原因：

1. JDK8中的ExtensionClassLoader主要用来加载`jre/lib/ext`下的`jar`包，当我们需要扩展`java`功能的时候，将需要扩展的`jar`包放到`jre/lib/ext`目录下,这种做法是不安全的
2. JDK9以后引入了模块化，对于ExtensionClassLoader这种扩展的机制就被模块化天然的扩展能力给取代了

除了Java虚拟机自带的加载器以外，用户也可以自定义加载器

用户自定义的加载器是`java.lang.ClassLoader`的子类

用户可以定制类的加载方式；只不过自定义类加载器的加载顺序是在所有系统类加载器的最后

##### 类加载器之间的关系

- `User ClassLoader(用户自定义类加载器) 的父级为 AppClassLoader`
- `AppClassLoader 的父级为 PlatformClassLoader(如果是JDK8的话就是ExtensionClassLoader)`
- `PlatformClassLoader 的父级为 BootstrapClassLoader（根）`

关系如图所示：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gmmemxpta5j31180n6al6.jpg" alt="image-20210113214815296" style="zoom:33%;" align="left"/>



#### 3-2 案例：类加载器使用

- 启动类加载器：

  用于加载启动的基础模块类，比如：`java.base`,`java.management`,`java.xml`等等

- 平台类加载器：

  用于加载一些平台相关的模块，比如：`java.scripting`,`java.compiler*`,`java.corba*`等等

- 应用程序类加载器：

  用于加载应用级别的模块，比如：`jdk.compiler`,`jdk.jartool`,`jdk.jshell`等等；还加载`classpath`路径中的所有类库

示例程序：

```java
package classloader;

import java.sql.Driver;

public class ClassLoaderStudy {
    public static void main(String[] args) throws ClassNotFoundException {
        String str = "Hello";
        // 输出结果为null,因为BootstrapClassLoader不允许为外部调用
        System.out.println("str class loader : " + str.getClass().getClassLoader());

        Class driver = Class.forName("java.sql.Driver");
        // PlatformClassLoader
        System.out.println("driver class loader : " + driver.getClassLoader());
        // PlatformClassLoader的父级为BootstrapClassLoader
        System.out.println("driver parent class loader : " + driver.getClassLoader().getParent());

        ClassLoaderStudy classLoaderStudy = new ClassLoaderStudy();
        // AppClassLoader
        System.out.println("classLoaderStudy class loader : " + classLoaderStudy.getClass().getClassLoader());
        // AppClassLoader的父级为PlatformClassLoader
        System.out.println("classLoaderStudy parent class loader : " + classLoaderStudy.getClass().getClassLoader().getParent());
        // PlatformClassLoader的父级为BootstrapClassLoader
        System.out.println("classLoaderStudy parent.parent class loader : " + classLoaderStudy.getClass().getClassLoader().getParent().getParent());

        // AppClassLoader 除了加载classpath路径中的类库，还要加载应用级别的模块 例如：jshell
        Class jshell = Class.forName("jdk.jshell.JShell");
        System.out.println("jshell class loader : " + jshell.getClassLoader());
    }
}
```

程序输出结果：

```
str class loader : null
driver class loader : jdk.internal.loader.ClassLoaders$PlatformClassLoader@5ebec15
driver parent class loader : null
classLoaderStudy class loader : jdk.internal.loader.ClassLoaders$AppClassLoader@512ddf17
classLoaderStudy parent class loader : jdk.internal.loader.ClassLoaders$PlatformClassLoader@5ebec15
classLoaderStudy parent.parent class loader : null
jshell class loader : jdk.internal.loader.ClassLoaders$AppClassLoader@512ddf17
```



对于JDK8中：

- 启动类加载器：

  负责将`<JAVA_HOME>/lib`，或者`-Xbootclasspath`参数指定的路径中的，且是虚拟机识别的类库加载到内存中(按照名字识别，比如`rt.jar`，对于不能识别的文件不予装载)

- 扩展类加载器：

  负责加载`<JRE_HOME>/lib/ext`，或者`java.ext.dirs`系统变量所指定路径中的所有类库

- 应用程序类加载器：

  负责加载`classpath`路径中的所有类库

##### 类加载器说明

1. Java程序不能直接引用启动类加载器，直接设置`classLoader`为`null`，默认就使用启动类加载器
2. 类加载器并不需要等到某个类“首次主动使用”的时候才加载它，JVM规范允许类加载器在预料到某个类将要被使用的时候就预先加载它
3. 如果在加载的时候`.class`文件缺失，会在该类首次主动使用的时候报`LinkageError`；如果一直没有被使用，就不会报错

#### 3-3 双亲委派模型

JVM中的`ClassLoader`通常采用**双亲委派模型**，要求除了启动类加载器外，其余的类加载器都应该有自己的父级加载器。这里的父子关系是组合而不是继承。工作过程如下：

1. 一个类加载器接收到类加载请求后，首先搜索它的内建加载器定义的所有“具名模块”
2. 如果找到了合适的模块定义，将会使用该类加载器来加载
3. 如果`class`没有在这些加载器定义的具名模块中找到，那么将会委托给父级加载器，直到启动类加载器
4. 如果父级加载器反馈它不能完成加载请求，比如在它的搜索路径下找不到这个类，那子的类加载器才自己来加载
5. 在类路径下找到的类将成为这些加载器的无名模块

##### 双亲委派模型说明

1. 双亲委派模型对于保证Java程序的稳定运作很重要
2. 实现双亲委派的代码在`java.lang.ClassLoader`的`loadClass()`方法中；如果自己定义类加载器的话，推荐覆盖实现`findClass()`方法







#### 3-4 案例：自定义你ClassLoader

#### 3-5 双亲委派模型说明和代码示例

#### 3-6 类连接和初始化

#### 3-7 案例：类的主动初始化

#### 3-8 案例：类的初始化机制和顺序