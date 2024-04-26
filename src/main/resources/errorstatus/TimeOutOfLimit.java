public class Main {
    public static void main(String[] args) {
        Thread thread = Thread.currentThread();
        try {
            thread.sleep(60 * 60 * 1000);
            System.out.println("sleep out");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}