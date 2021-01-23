package section6;

public class TestStack {
    private int num = 0;

    private int callMe(int a, int b) {
        num++;
        return callMe(a + num, b + num);
    }

    public static void main(String[] args) {
        TestStack testStack = new TestStack();
        try {
            testStack.callMe(1, 2);
        } catch (Throwable e) {
            System.out.println("call times = " + testStack.num);
            e.printStackTrace();
        }
    }
}
