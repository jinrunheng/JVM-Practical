package classinit;

import java.lang.reflect.InvocationTargetException;

public class Test2 {
    static{
        System.out.println("Test2 class init");
    }
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        // 类的主动初始化
        // 1. 创建类的实例
        // MyParent myParent = new MyParent();
        // 2. 访问某个类或接口的静态变量
        // System.out.println(MyParent.staticVariable);
        // 3. 调用类的静态方法
        // MyParent.p();
        // 4. 反射
        // Class cls = Class.forName("classinit.MyParent");
        // 5. 初始化某个类的子类，会主动初始化父类
        // MyChild myChild = new MyChild();
        // 6. JVM启动的时候运行的主类
        // 运行main方法首先会初始化Test2这个类
        // 7. 定义了default方法的接口，当接口实现类初始化时
        // MyInterface myInterface = (MyInterface)Class.forName("classinit.MyChild").getConstructor().newInstance();
    }
}
