import cn.hutool.core.io.FileUtil;

public class Main {
    public static void main(String[] args) {
        String fileName = "aa.bat";
        String property = System.getProperty("user.dir");
        String filePath = property + FileUtil.FILE_SEPARATOR + "src/main/resources" +
                FileUtil.FILE_SEPARATOR + fileName;
        FileUtil.newFile(filePath);
    }
}
