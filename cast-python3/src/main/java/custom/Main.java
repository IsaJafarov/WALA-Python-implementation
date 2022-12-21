package custom;

public class Main {

    public static void main(String[] args) throws Exception {
        MyClass myClass = new MyClass();
        try {
            myClass.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
