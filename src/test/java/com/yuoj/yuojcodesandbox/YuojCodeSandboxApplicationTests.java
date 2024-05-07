package com.yuoj.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import com.yuoj.yuojcodesandbox.judge.CPPNativeCodeSandBox;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import com.yuoj.yuojcodesandbox.utils.ProcessUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class YuojCodeSandboxApplicationTests {
    
    @Resource
    private CPPNativeCodeSandBox cppNativeCodeSandBox;
    
    @Test
    void contextLoads() {
    }
    
    @Test
    void textCppCode(){
        String cppCode = "#include <iostream>\n" +
                "int main() {\n" +
                "    int num1, num2, sum;\n" +
                "    std::cin >> num1;\n" +
                "    std::cin >> num2;\n" +
                "    sum = num1 + num2;\n" +
                "    std::cout << \"Sum = \" << sum << std::endl;\n" +
                "    return 0;\n" +
                "}\n";
        File cppFile = cppNativeCodeSandBox.saveCodeToFile(cppCode);
        String userCodePath = cppFile.getAbsolutePath();
        String userCodeParentPath = cppFile.getParentFile().getAbsolutePath();
        cppNativeCodeSandBox.compileFile(cppFile,String.format("g++ -finput-charset=UTF-8 -fexec-charset=UTF-8 %s -o %s", userCodePath, userCodeParentPath+FileUtil.FILE_SEPARATOR+"hello"));

        List<String> inputList = Arrays.asList("1 2","3 4");
        List<ExecuteMessage> executeMessageList = ProcessUtils.executeCppCode(inputList, cppFile.getParentFile().getAbsolutePath() + FileUtil.FILE_SEPARATOR + "hello");
        System.out.println(executeMessageList);
    }
    
}
