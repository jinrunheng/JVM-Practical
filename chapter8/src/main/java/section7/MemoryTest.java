package section7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MemoryTest {

    public static void main(String[] args) {

        try {
            Thread.sleep(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<A> list = new ArrayList<>();
        for(int i = 0; i < 10000; i++){
            list.add(new A());

            if(i % 20 == 0){
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        System.gc();
        System.out.println("over");
        // 阻塞
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class A {
    private byte[] bs = new byte[10 * 1024];
}
