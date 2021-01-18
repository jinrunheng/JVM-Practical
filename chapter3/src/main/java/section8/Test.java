package section8;

public class Test {
    public static void main(String[] args) {
        // 子类使用父类的静态变量，不会导致子类的初始化
        System.out.println(Child.parent);
        // 用数组定义来引用类，不会导致类的初始化
        Child[] children = new Child[10];
        // 访问常量不会导致类的初始化
        System.out.println(Child.CONSTANT);
    }
}
