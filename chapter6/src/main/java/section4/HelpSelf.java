package section4;

public class HelpSelf {
    private static HelpSelf hs = null;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("now in finalize");
        // 对象自救
        hs = this;
    }

    public static void main(String[] args) throws InterruptedException {
        hs = new HelpSelf();

        // 第一次
        hs = null;
        System.gc();
        // System.gc()不一定会垃圾回收，添加线程sleep方法，增加GC的可行性
        Thread.sleep(1000L);
        System.out.println("first hs : " + hs);

        // 第二次
        hs = null;
        System.gc();
        Thread.sleep(1000L);
        System.out.println("first hs : " + hs);
    }
}
