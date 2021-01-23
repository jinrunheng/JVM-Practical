package section3;

public class TestOperandStack {
    public int add(int a, int b) {
        int c = a + b;
        return c;
    }

    public static void main(String[] args) {
        TestOperandStack test = new TestOperandStack();
        int res = test.add(1, 2);
        System.out.println("res = " + res);
    }
}
