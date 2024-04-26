package com.yuoj.yuojcodesandbox.errorcode;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.io.IOException;

public class ExecFile {
    public static void main(String[] args) throws IOException {
        String fileName = "aa.bat";
        String property = System.getProperty("user.dir");
        String filePath = property + FileUtil.FILE_SEPARATOR + "src/main/resources" + FileUtil.FILE_SEPARATOR +
                fileName;
        File file = new File(filePath);
        file.createNewFile();
    }
}
