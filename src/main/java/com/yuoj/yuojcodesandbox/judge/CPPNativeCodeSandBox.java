package com.yuoj.yuojcodesandbox.judge;

import cn.hutool.core.io.FileUtil;
import com.yuoj.yuojcodesandbox.judge.model.CodeSandCmd;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeRequest;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeResponse;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import com.yuoj.yuojcodesandbox.utils.ProcessUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CPPNativeCodeSandBox extends CodeSandBoxTemplate {
    
    public String GLOBAL_FILE_NAME = System.getProperty("user.dir") + FileUtil.FILE_SEPARATOR + "tempCode";
    public String CPP_CODE_NAME = "main.cpp";
    
    public CPPNativeCodeSandBox(){
        super.GLOBAL_LANGUAGE_NAME = CPP_CODE_NAME;
        super.GLOBAL_FILE_NAME = GLOBAL_FILE_NAME;
    }
    
    @Override
    CodeSandCmd getCmd(String userCodeParentPath, String userCodePath) {
        return CodeSandCmd.builder()
                .compileCmd(String.format("g++ -finput-charset=UTF-8 -fexec-charset=UTF-8 %s -o %s", userCodePath, userCodeParentPath+FileUtil.FILE_SEPARATOR+"hello"))
                .runCmd(userCodeParentPath+FileUtil.FILE_SEPARATOR+"hello")
                .build();
    }

    @Override
    public List<ExecuteMessage> runCodeFile(File cppFile, List<String> inputList, String runCmd) {
        List<ExecuteMessage> outputlist = ProcessUtils.executeCppCode(inputList, cppFile.getParentFile().getAbsolutePath() + FileUtil.FILE_SEPARATOR + "hello");
        return outputlist;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
