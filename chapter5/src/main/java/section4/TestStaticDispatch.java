package section4;

public class TestStaticDispatch {
    public void t1(int a){
        System.out.println("t1(int) a = " + a);
    }

    public void t1(String a){
        System.out.println("t1(String) a = " + a);
    }

    public static void main(String[] args) {
        TestStaticDispatch t = new TestStaticDispatch();
        t.t1("s");
        t.t1(5);
    }
}
