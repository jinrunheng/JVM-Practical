## 第三章 类加载、连接和初始化

#### 3-1 类加载和类加载器

##### 类加载过程

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

3. 如果有一个类加载器能加载某个类，成为定义类加载器；所有能成功返回该类的`Class`的类加载器都被称为初始类加载器
4. 如果没有指定父加载器，默认就是启动加载器
5. 每个类加载器都有自己的命名空间，命名空间由该加载器及其所有父加载器所加载的类构成，不同的命名空间可以出现类的全路径名相同的情况。
6. 运行时包由同一个类加载器的类构成，决定两个类是否属于同一个运行时包，不仅要看全路径名是否一样，还要看定义类加载器是否相同。只有属于同一个运行时包的类才能实现相互包内可见。

#### 3-4 案例：自定义ClassLoader

自定义`ClassLoader`需要覆盖实现`findClass()`方法

自定义类加载器

**MyClassLoader**

```java
package classloader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class MyClassLoader extends ClassLoader {

    private String myName = "";

    public MyClassLoader(String myName) {
        this.myName = myName;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] data = this.loadClassData(name);
        return this.defineClass(name, data, 0, data.length);
    }

    private byte[] loadClassData(String className) {
        byte[] data = null;
        InputStream in = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        className = className.replace(".", "/");
        try (out) {
            in = new FileInputStream(new File("classes/" + className + ".class"));
            int b = 0;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            data = out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
```

**MyClass**

```java
package classloader;
public class MyClass {

    public void m(){
        System.out.println("now in MyClass : m()");
    }
}

```

**UseMyClassLoader**

```java
package classloader;

public class UseMyClassLoader {
    public static void main(String[] args) throws ClassNotFoundException {
        MyClassLoader myClassLoader = new MyClassLoader("myClassLoader1");
        Class cls1 = myClassLoader.loadClass("classloader.MyClass");
        System.out.println("cls1 class loader : " + cls1.getClassLoader());
        System.out.println("cls1 parent class loader : " + cls1.getClassLoader().getParent());
    }
}
```

运行`UseMyClassLoader`里的`main`方法;结果为：

```
cls1 class loader : jdk.internal.loader.ClassLoaders$AppClassLoader@512ddf17
cls1 parent class loader : jdk.internal.loader.ClassLoaders$PlatformClassLoader@2acf57e3
```

我们此时发现，加载`cls1`的类加载器并不是我们自己定义的`MyClassLoader`;究其原因，是因为类加载遵循双亲委派模型。

如果一个类加载器受到了类加载的请求，它并不会自己先去加载，而是将这个加载类的请求委托给父加载器，由父加载器去加载，如果父加载器在其搜索路径下找到了这个类，那么就由父加载器去加载这个类，否则就一直向上找，直到找到启动类加载器；如果到启动类加载器都无法完成加载，那么子加载器才会尝试自己去加载，这就是双亲委派模型。

因为我们自己定义的类加载器属于自定义类加载器，它会将加载请求委托给它的父级加载器：`AppClassLoader`;而我们的编译器编译完成后，在`target`目录下正好可以找到`MyClass.class`文件，所以将会由`AppClassLoader`加载。

我们可以将编译后的路径下的`MyClass.class`复制并删除，粘贴到我们自己定义的目录下

再次运行代码,我们就可以看到我们期望看到的输出结果了：

```
cls1 class loader : classloader.MyClassLoader@56cbfb61
cls1 parent class loader : jdk.internal.loader.ClassLoaders$AppClassLoader@512ddf17
```

#### 3-5 双亲委派模型说明和代码示例

##### 双亲委派模型说明

1. 双亲委派模型对于保证Java程序的稳定运作很重要
2. 实现双亲委派的代码在`java.lang.ClassLoader`的`loadClass()`方法中；如果自己定义类加载器的话，推荐覆盖实现`findClass()`方法

3. 如果有一个类加载器能加载某个类，成为定义类加载器；所有能成功返回该类的`Class`的类加载器都被称为初始类加载器
4. 如果没有指定父加载器，默认就是启动加载器
5. 每个类加载器都有自己的命名空间，命名空间由该加载器及其所有父加载器所加载的类构成，不同的命名空间可以出现类的全路径名相同的情况。
6. 运行时包由同一个类加载器的类构成，决定两个类是否属于同一个运行时包，不仅要看全路径名是否一样，还要看定义类加载器是否相同。只有属于同一个运行时包的类才能实现相互包内可见。

##### 破坏双亲委派模型

- 双亲委派模型有个问题：父加载器无法向下识别子加载器加载的资源

- 为了解决这个问题，引入了线程上下文类加载器，可以通过`Thread`的 `setContextClassLoader()`进行设置

  典型实例：

  我们先来看一个代码

  ```java
  public class Test {
      public static void main(String[] args) throws ClassNotFoundException {
          Class driverManagerClass = Class.forName("java.sql.DriverManager");
          Class mysqlDriverClass = Class.forName("com.mysql.cj.jdbc.Driver");
  
          System.out.println("driverManagerClass classloader : " + driverManagerClass.getClassLoader());
          System.out.println("mysqlDriverClass classloader : " + mysqlDriverClass.getClassLoader());
      }
  }
  ```

  程序输出：

  ```
  driverManagerClass classloader : jdk.internal.loader.ClassLoaders$PlatformClassLoader@6767c1fc
  mysqlDriverClass classloader : jdk.internal.loader.ClassLoaders$AppClassLoader@512ddf17
  ```

  我们知道我们需要用`DriverManager`来加载`mysql`的驱动,但是`java.sql.DriverManager`是由`PlatformClassLoader`加载的，而`com.mysql.cj.jdbc.Driver`是由`AppClassLoader`加载的；因为双亲委派机制，只能由子加载器一直向上找，按理来讲`DriverManger`无法访问到`mysql`驱动的资源。

  所以说，双亲委派模型是“子找父”；有的时候，我们也需要“父找子”这样的情况；这种情况我们就需要破坏双亲委派模型。

  Java使用了一种解决方式，将需要加载的类加载器的引用存放在线程上下文加载器中，在任何需要的时候使用：`Thread.currentThread().getContextClassLoader()`取出即可

  我们来看下`java.sql.DriverManager`中的`getConnection`中的一段代码

  ```java
  ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
  if (callerCL == null || callerCL == ClassLoader.getPlatformClassLoader()) {
      callerCL = Thread.currentThread().getContextClassLoader();
  }
  ```

  在`ContextClassLoader`中存放了`AppClassLoader`的引用，等到我们需要加载`mysql`的`driver`时，在从线程中获取上下文加载器即可。

- 另外一种典型情况就是实现热替换，比如OSGI的模块化热部署，它的类加载器就不再是严格按照双亲委派模型，很多可能就在平级的类加载器中执行了。

#### 3-6 类连接和初始化

##### 类连接主要分为三个部分

- 验证
- 准备
- 解析

###### 验证

- 类文件结构检查：按照JVM规范规定的类文件结构进行
- 元数据验证：对字节码描述的信息进行语义分析，保证其符合Java语言规范要求
- 字节码验证：通过对数据流和控制流进行分析，确保程序语义说合法和符合逻辑的。这里主要对方法进行校验
- 符号引用验证：对类自身以外的信息，也就是常量池中的各种符号引用，进行匹配校验

###### 准备

- 为类的**静态变量**分配内存

###### 解析

所谓解析就是将常量池中的符号引用转换成直接引用的过程

- 符号引用：以一组无歧义的符号来描述所引用的目标，与虚拟机的实现无关
- 直接引用：直接指向目标的指针，相对偏移量，或是能间接定位到目标的句柄，和虚拟机实现相关的 

解析主要针对：类，接口，字段，类方法，接口方法，方法类型，方法句柄，调用点限定符



##### 类的初始化

- 类的初始化就是为类的静态变量赋初始值，或者说是执行类构造器`<clinit>`方法的过程
  - 如果类还没有加载和连接，就先加载和连接
  - 如果类存在父类，且父类没有初始化，就先初始化父类
  - 如果类中存在初始化语句，就依次执行这些初始化语句
  - 如果是接口：
    - 初始化一个类的时候，并不会先初始化它实现的接口
    - 初始化一个接口的时候，并不会初始化它的父接口
    - 只有当程序首次使用接口里面的变量或者是调用接口方法的时候，才会导致接口初始化
  - 调用`ClassLoader`类的`loadClass`方法来装载一个类，并不会初始化这个类，这不是对类的主动使用

#### 3-7 案例：类的主动初始化

##### 类的初始化时机

Java程序对类的使用方式分为：**主动使用**和**被动使用**

JVM必须在每个类或接口"首次主动使用"时才初始化它们；被动使用类不会导致类的初始化

主动使用的情况：

- 创建类的实例

- 访问某个类或接口的静态变量

- 调用类的静态方法

- 反射

- 初始化某个类的子类，会主动初始化父类

- JVM启动的时候运行的主类

- 定义了`default`方法的接口，当接口实现类初始化时

  比如一个接口实现了方法：

  ```java
  default void defaultMethod(){
      System.out.println("default");
  }
  ```

  一个实现接口的类初始化时，该接口因为定义了`default`方法，所以当实现接口的类初始化的时候，接口也会随着初始化

#### 3-8 案例：类的初始化机制和顺序

我们来看一个程序示例：

**MyTestClass**

```java
public class MyTestClass {
    private static MyTestClass myTestClass = new MyTestClass();

    private static int a = 0;
    private static int b;

    private MyTestClass() {
        a++;
        b++;
    }

    public static MyTestClass getInstance() {
        return myTestClass;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }
}
```

**Test3**

```java
public class Test3 {

    public static void main(String[] args) {
        MyTestClass myTestClass = MyTestClass.getInstance();
        System.out.println("myTestClass.a : " + myTestClass.getA());
        System.out.println("myTestClass.b : " + myTestClass.getB());

    }
}
```

请问程序输出的结果？

这个问题涉及到了类的初始化顺序

先来看看答案：

```
myTestClass.a : 0
myTestClass.b : 1
```

为什么出现这样的结果呢？

我们再次回顾下类的加载过程：

```
加载 -> 连接(验证 -> 准备 -> 解析) -> 初始化 -> 使用 -> 卸载
```

首先在**连接的准备阶段**，JVM会为类的**静态变量**分配内存，并赋**缺省值**，即：

```
myTestClass = null；
a = 0;
b = 0;
```

接着，在类的**初始化**阶段，会为这些静态变量赋初始值

```java
myTestClass = new MyTestClass();
```

这句话会回调构造器

```java
private MyTestClass() {
    a++;
    b++;
}
```

让`a++`,`b++`;导致`a`和`b`的结果均为`1`

然后代码执行到：

```java
a = 0;
b;
```

这个时候，执行对`a`和`b`真正的初始化赋值

又将`a`变为了`0`；而`b`则没有赋值结果仍然是`1`；所以输出结果为

```
myTestClass.a : 0
myTestClass.b : 1
```

我们再来看一个程序：

**MyTestClass2**

```java
public class MyTestClass2 {

    private static int a = 0;
    private static int b;

    private MyTestClass2(){
        a++;
        b++;
    }

    private static final MyTestClass2 myTestClass2 = new MyTestClass2();

    public static MyTestClass2 getInstance(){
        return myTestClass2;
    }
}
```

**Test4**

```java
public class Test4 {
    public static void main(String[] args) {
        MyTestClass2 myTestClass2 = MyTestClass2.getInstance();
        System.out.println("myTestClass2.a : " + myTestClass2.getA());
        System.out.println("myTestClass2.b : " + myTestClass2.getB());
    }
}
```

那么这个程序运行的结果为多少呢？

结果为：

```
myTestClass2.a : 1
myTestClass2.b : 1
```

我们再次按照类的初始化顺序进行分析：

首先在**连接的准备阶段**，JVM会为类的**静态变量**分配内存，并赋**缺省值**，即：

```
a = 0;
b = 0;
myTestClass2 = null;
```

然后，在类的**初始化**阶段，会为这些静态变量赋初始值

首先，代码执行到：

```
a = 0;
b;
```

`a`赋初始值为`0`,然后`b`没有赋值，其结果还是`0`

接着，执行到：

```java
myTestClass = new MyTestClass();
```

这句话会回调构造器

```java
private MyTestClass() {
    a++;
    b++;
}
```

让`a++`,`b++`;导致`a`和`b`的结果均为`1`；所以最终输出的结果为：

```
myTestClass2.a : 1
myTestClass2.b : 1
```



##### 类的被动使用不会导致初始化

我们介绍了几种主动使用类，导致类的初始化的方法：

主动使用的情况：

- 创建类的实例

- 访问某个类或接口的静态变量

- 调用类的静态方法

- 反射

- 初始化某个类的子类，会主动初始化父类

- JVM启动的时候运行的主类

- 定义了`default`方法的接口，当接口实现类初始化时

  比如一个接口实现了方法：

  ```java
  default void defaultMethod(){
      System.out.println("default");
  }
  ```

  一个实现接口的类初始化时，该接口因为定义了`default`方法，所以当实现接口的类初始化的时候，接口也会随着初始化

在被动使用类的时候，则不会导致类的初始化：

被动使用：

- 子类使用父类的静态变量，不会导致父类的初始化

- 

  