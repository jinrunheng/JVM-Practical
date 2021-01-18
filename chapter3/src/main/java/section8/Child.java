package section8;

public class Child extends Parent{
    public static final String CONSTANT = "HELLO";
    static {
        System.out.println("Child class init");
    }
}
