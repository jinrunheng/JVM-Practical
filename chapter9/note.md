## 第九章 JVM调优

#### 9-1 JVM调优：调什么？如何调？

##### 调什么

- 内存方面
  - *JVM* 需要的内存总大小
  - 各块内存的分配，新生代，老年代，存活区
  - 选择合适的垃圾回收算法，控制 *GC* 停顿次数和时间
  - 解决内存泄漏问题，辅助代码优化
  - 内存热点：检查哪些对象在系统中数量最大，辅助代码优化
- 线程方面
  - 死锁检查，辅助代码优化
  - *Dump* 线程详细信息，查看线程内部运行情况，查找竞争线程，辅助代码优化
  - *CPU* 热点：检查系统哪些方法占用了大量 *CPU* 时间，辅助代码优化

##### 如何调优

- 监控 *JVM* 状态，主要是内存，线程，代码，*I/O* 这几个部分
- 分析结果，判断是否需要优化
- 调整，垃圾回收算法和内存分配；修改并优化代码
- 不断重复监控，分析和调整，直至找到优化的平衡点

#### 9-2 调优的目标，调优的策略和调优冷思考

##### 调优的目标

- *GC* 的时间足够小
- *GC* 的次数足够的少
- 将转移到老年代的对象数量降低到最小
- 减少 *Full GC* 的执行时间
- 发生 *Full GC* 的间隔足够的长

##### 常见的调优策略

- 减少创建对象的数量
- 减少使用全局变量和大对象
- 调整新生代，老年代的大小到最合适
- 选择合适的 *GC* 收集器，并设置合理的参数

##### JVM调优冷思考

- 多数的 *Java* 应用不需要在服务器上进行 *GC* 优化
- 多数导致 *GC* 问题的 *Java* 应用，都不是因为参数设置错误，而是代码问题
- 在应用上线前，先考虑将机器的 *JVM* 参数设置到最优（最适合）
- *JVM* 优化上到最后不得已才采用的手段
- 在实际使用中，分析 *JVM* 情况优化代码比优化 *JVM* 本身要多得多
- 如下情况，通常不需要进行优化：
  - *Minor GC* 执行时间不到 *50ms*
  - *Minor GC* 执行时间不频繁，约 *10s* 一次
  - *Full GC* 执行时间不到 *1s*
  - *Full GC* 执行频率不算频繁，不低于 *10* 分钟 *1* 次

#### 9-3 JVM调优经验，内存泄漏分析

##### JVM调优经验

- 要注意 *32* 位和 *64* 位机的区别，通常 *32* 位的仅支持 *2 - 3g* 左右的内存
- 要注意 *Client* 模式和 *Server* 模式的选择
- 要想 *GC* 时间小必须要一个更小的堆；而要保证 *GC* 次数足够少，又必须保证一个更大的堆，这两个是有冲突的，只能取其平衡
- 针对 *JVM* 堆堆设置，一般可以通过 *-Xms -Xmx* 限定其最小，最大值，为了防止垃圾收集器在最小，最大之间收缩堆而产生额外的时间，通常把最大，最小设置为相同值
- 新生代和老年代将根据默认的比例（*1 ： 2*）分配堆内存，可以通过调整二者之间的比率 *NewRatio* 来调整，也可以通过 *-XX:newSize -XX:MaxNewSize* 来设置其绝对大小，同样，为了防止新生的堆收缩，通常会把 *-XX:newSize -XX:MaxNewSize* 设置为同样大小
- 合理规划新生代和老年代的大小
- 如果应用存在大量的临时对象，应该选择更大的新生代；如果存在相对较多的持久对象，老年代应该适合增大。在抉择时应该本着 *Full GC* 尽量少的原则，让老年代尽量缓存常用对象，*JVM* 的默认比例 *1 ：2* 也是这个道理
- 通过观察应用一段时间，看其在峰值时老年代会占多少内存，在不影响 *Full GC* 的前提下，根据实际情况加大新生代，但应该给老年代至少预留 *1/3* 的增长空间
- 线程堆栈的设置：每个线程默认会开启 *1M* 的堆栈，用于存放栈帧，调用参数，局部变量等，对大多数应用而言，这个默认值太大了，一般 *256K* 就足够用。在内存不变的情况下，减少每个线程的堆栈，可以产生更多的线程

##### 内存泄漏的分析处理

内存泄漏导致系统崩溃前的一些现象，比如：

- 每次垃圾回收的时间越来越长，*Full GC* 时间也延长到好几秒

- *Full GC* 的次数越来越多，最频繁时隔不到 *1* 分钟就进行一次 *Full GC*

- 老年代的呢你村越来越大，并且每次 *Full GC* 后老年代没有内存被释放

- 老年代堆空间被占满的情况

  - 这种情况的解决方式：一般就是根据垃圾回收前后情况对比，同时根据对象引用情况分析，辅助去查找泄漏点

- 堆栈溢出的情况

  通常抛出 *java.lang.StackOverflowError* 例外

  一般就是递归调用没退出，或者循环调用造成的

#### 9-4 JVM调优实战：认识待调优的应用

调优实战

- 重点是调优的过程，方法和思路
- 内存调整，数据库连接调整，内存泄漏查找

#### 9-5 JVM调优实战：录制JFR并分析结果

*jmc* 建立连接：

- 主机：*192.168.1.113*
- 端口：*6666*

*VisualVM* 建立连接：

- 主机：*192.168.1.113*
- 端口：*6666*

运行项目，在 *jmc* 监控下的 *CPU* 与 内存变化结果如图所示：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn75lkmhbqj30y40hcdqz.jpg" alt="image-20210131203227642" style="zoom:50%;" align="left"/>

我们可以看到，*CPU* 的占用率一直为 *100%*；并且在内存分析中，我们看到，出现了频繁 *GC* 的情况

*VisualVM* 反馈的结果也是完全一致的：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn75o1r0t2j30y60hy46x.jpg" alt="image-20210131203456975" style="zoom:50%;" align="left"/>

在对 *jmc* 的飞行记录器分析中我们可以发现：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn75rm1ko9j30y40gwws2.jpg" alt="image-20210131203824416" style="zoom:50%;" align="left"/>

*2 min* 的飞行记录，*G1 New* 发生了 *70* 多次 *GC* ！这很有可能因为新生代分到的内存太小，所以导致这么频繁的发生 *GC*。

修改 *VM options* 参数：

```
-Xms800m -Xmx800m -Xmn350m
```

将新生代分配的内存扩大至 *350m*，重新开启 *jmc* *2min* 的飞行记录

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn761554atj30y80gsk54.jpg" alt="image-20210131204716525" style="zoom:50%;" align="left"/>

我们看到 *GC* 的次数从原来的的 *70* 多次降低到了 *50* 多次，最大的暂停时长为 *80* 多毫秒，这种程度的优化还是远远不够的。

#### 9-6 JVM调优实战：按照分析结果调整JVM运行时内存参数

我们再次改动 *VM options* 参数：

```
-Xms1000m -Xmx1000m -Xmn500m
```

将堆内存扩大至 *1G* 左右，新生代分配 *500M* 和老年代的比例为 *1 ：1*

数据库部分的优化：

通过分析 *Druid* ，分析数据库等待连接次数高达：*38939* 次；说明数据库连接设置过小

设置数据库的连接池个数为 *100*

```
set GLOBAL max_connections=100
```

重启 *Tomcat*

飞行记录器的 *2min* 记录结果如下：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn76j6e28jj30y60gaqh8.jpg" alt="image-20210131210450597" style="zoom:50%;" align="left"/>

我们看到，每次 *GC* 的最长暂停从原来的 *80* 多毫秒提升到了 *20* 多毫秒

我们继续调大数据库连接池个数为 *1000*：

```
set GLOBAL max_connections=1000
```

#### 9-7 JVM调优实战：查找内存泄漏点，分析并处理内存泄漏

对程序的内存泄漏分析也是对 *JVM*调优重要的手段

我人为地在项目中添加内存泄漏点：

*GoodService*

```java

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cc.common.service.BaseService;
import com.cc.goodsmgr.dao.GoodsDAO;
import com.cc.goodsmgr.vo.GoodsModel;
import com.cc.goodsmgr.vo.GoodsQueryModel;
import com.cc.pageutil.Page;

@Service
@Transactional
public class GoodsService extends BaseService<GoodsModel,GoodsQueryModel> implements IGoodsService{
	private GoodsDAO dao = null;
	@Autowired
	private void setDao(GoodsDAO dao){
		this.dao = dao;
		super.setDAO(dao);
	}
	@Override
	public Page<GoodsModel> getByConditionPage(GoodsQueryModel qm){
		List<GoodsModel> list = dao.getByConditionPage(qm);
		qm.getPage().setResult(list);
		
    // 添加内存泄漏点
		MyData.addList();
		
		return qm.getPage();
	}
}
```

*MyData*

```java
import java.util.ArrayList;
import java.util.List;

public class MyData {
	private static List<MyModel> list = new ArrayList<MyModel>();
	
	public static void addList() {
		list.add(new MyModel());
	}
}
```

*MyModel*

```java
public class MyModel {
	private byte[] bs = new byte[100*1024];
}
```

重新启动项目，观察 *MBean* 的变化趋势：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn76ztsaf0j30y60fq12x.jpg" alt="image-20210131212035114" style="zoom:50%;" align="left"/>

我们可以看到，堆内存中有特别明显的上升趋势，已经撑到了我们设置的堆最大内存空间

对于这种情况，我们需要怎样分析？

我们可以打开 *VisualVM* 的对象占用内存情况：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn7732h7kuj30y40h6woy.jpg" alt="image-20210131212402531" style="zoom:50%;" align="left"/>

我们看到最高的占用内存就是 *byte[]* 数组，我们可以对程序跟踪所有涵盖 *byte[]* 数组的地方，逐一分析，就可以找到内存泄漏源。

当然，更简单的手段就是录制 *JFR* ，看飞行记录结果进行分析：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn777b7drcj30ya0getjg.jpg" alt="image-20210131212805530" style="zoom:50%;" align="left"/>

同样地，通过 *JFR* 我们可以看到，*byte[]* 数组占用的内存为 *1.13G*，内存逐步升高，很有可能发生了内存泄漏。

看内存部分：

<img src="https://tva1.sinaimg.cn/large/008eGmZEgy1gn77ctnbzsj30ya0huna5.jpg" alt="image-20210131213208583" style="zoom:50%;" align="left"/>

我们通过堆栈跟踪，就可以看到是哪个类，哪个方法，出现了内存泄漏的可能。