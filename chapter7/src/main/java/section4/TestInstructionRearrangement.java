package section4;

public class TestInstructionRearrangement {
    {
        int a = 0;
        int b = 0;
        // 可以重排
        a = 1;
        b = 2;
    }
    {
        int a = 0;
        int b = 0;
        a = 1;
        b = 2;
        // 写后读无法重排
        int c = a + b;
    }
    {
        int a = 0;
        // 写后写无法重排
        a = 1;
        a = 2;
    }
    {
        int b = 0;
        int a = 0;
        // 读后写不能重排
        a = b;
        b = 2;
    }

}
