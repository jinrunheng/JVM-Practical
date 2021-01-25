package section3;

public class User {
    // private byte[] bytes = new byte[10 * 1024];
    private String name;

    public User(String name){
        this.name = name;
    }

    @Override
    public String toString() {
        return "User name : " + name;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("now finalize user : " + name);
    }
}
