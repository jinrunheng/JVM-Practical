package section3.memory;


import java.util.ArrayList;
import java.util.List;

public class Test1 {
    private byte[] bs = new byte[1024 * 1024]; // 1MB

    public static void main(String[] args) {
        List<Test1> list = new ArrayList<>();
        int num = 0;
        try {
            while (true) {
                list.add(new Test1());
                num++;
            }
        } catch (Throwable err) {
            System.out.println("error, num = " + num);
            err.printStackTrace();
        }


        System.out.println("totalMemory = " + Runtime.getRuntime().totalMemory() / 1024.0 / 1024.0 + "MB");
        System.out.println("freeMemory = " + Runtime.getRuntime().freeMemory() / 1024.0 / 1024.0 + "MB");
        System.out.println("maxMemory = " + Runtime.getRuntime().maxMemory() / 1024.0 / 1024.0 + "MB");
    }
}
