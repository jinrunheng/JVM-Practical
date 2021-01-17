package classinit;

public class MyChild extends MyParent implements MyInterface{
    static {
        System.out.println("MyChild class init");
    }
    {
        System.out.println("MyChild block");
    }
    @Override
    public void m() {
        System.out.println("now in MyChild m()");
    }
}
