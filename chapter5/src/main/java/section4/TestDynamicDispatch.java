package section4;

public class TestDynamicDispatch extends TestStaticDispatch{
    @Override
    public void t1(String a) {
        System.out.println("TestDynamicDispatch t1(String) a = " + a);
    }

    public static void main(String[] args) {
        TestStaticDispatch t = new TestDynamicDispatch();
        t.t1("hello");
    }
}
