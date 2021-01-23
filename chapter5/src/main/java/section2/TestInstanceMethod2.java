package section2;

public class TestInstanceMethod2 {

    public static void main(String[] args) {

        {
            byte[] bytes = new byte[2 * 1024 * 1024]; // 2MB
        }
        // int a = 5; // 加上这句话，插槽复用，freeMemory反而会变大
        System.gc();

        System.out.println("totalMemory : " + Runtime.getRuntime().totalMemory());
        System.out.println("freeMemory : " + Runtime.getRuntime().freeMemory());
        System.out.println("maxMemory : " + Runtime.getRuntime().maxMemory());
    }
}
