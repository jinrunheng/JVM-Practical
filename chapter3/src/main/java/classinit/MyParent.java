package classinit;

public class MyParent {
    public static int staticVariable = 5;
    public static void p(){
        System.out.println("MyParent static method is invoke");
    }
    static {
        System.out.println("MyParent class init");
    }
    {
        System.out.println("MyParent block");
    }
}
