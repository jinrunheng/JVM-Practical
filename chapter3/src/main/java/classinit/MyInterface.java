package classinit;

public interface MyInterface {
    String myInterface = "myInterface";
    void m();
    default void defaultMethod(){
        System.out.println("default");
    }
}
