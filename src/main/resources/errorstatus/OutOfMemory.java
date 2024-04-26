import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<byte[]> list = new ArrayList<>();
        while (true){
            byte[] bytes = new byte[10000000];
            list.add(bytes);
        }
    }
}