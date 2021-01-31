package section5;

public class TestInstructionRearrangement {
    private static int x = 0;
    private static int y = 0;
    private static int a = 0;
    private static int b = 0;

    public static void main(String[] args) throws InterruptedException {
        // case1 ： t1 先运行完，然后 t2 再运行  结果为 a = 1,b = 2,x = 0,y = 1
        // case2 ： t2 先运行完，然后 t1 再运行  结果为 a = 1,b = 2,x = 2,y = 0
        // case3 ： t1,t2交叉运行；t1先运行部分，然后t2运行完，t1再运行剩余部分 结果为 a = 1,b = 2,x = 2,y = 1
        // case4 ： t1,t2交叉运行；t2先运行部分，然后t1运行完，t2再运行剩余部分 结果为 a = 1,b = 2,x = 2,y = 1
        // case5 ： t1,t2交叉运行；t1先部分运行，接着t2也部分运行，然后t1运行完，最后t2运行完 结果为 a = 1,b = 2,x = 2,y = 1
        // case6 ： t1,t2交叉运行；t2先部分运行，接着t1也部分运行，然后t2运行完，最后t1运行完 结果为 a = 1,b = 2,x = 2,y = 1

        // 重排的情况下，可能会出现 ： x = 0, y = 0 的情况
        for(int i = 0; i < 10000; i++){
             a = 0;
             b = 0;
             x = 0;
             y = 0;

            Thread t1 = new Thread(() -> {
                try {
                    if(System.currentTimeMillis() % 8 == 0){
                        Thread.sleep(1);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                a = 1;
                x = b;
            });

            Thread t2 = new Thread(() -> {
                b = 2;
                y = a;
            });

            t1.start();
            t2.start();
            t1.join();
            t2.join();

            System.out.println("i = " + i +  " x = " + x + " y = " + y);
        }
    }
}
