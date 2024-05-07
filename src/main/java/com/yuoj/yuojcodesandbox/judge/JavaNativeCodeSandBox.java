package com.yuoj.yuojcodesandbox.judge;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.yuoj.yuojcodesandbox.judge.model.*;
import com.yuoj.yuojcodesandbox.security.DefaultSecurityManager;
import com.yuoj.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class JavaNativeCodeSandBox extends CodeSandBoxTemplate {
    public String GLOBAL_FILE_NAME = System.getProperty("user.dir") + FileUtil.FILE_SEPARATOR + "tempCode";
    public String JAVA_CODE_NAME = "Main.java";
    
    public JavaNativeCodeSandBox(){
        super.GLOBAL_LANGUAGE_NAME = JAVA_CODE_NAME;
        super.GLOBAL_FILE_NAME = GLOBAL_FILE_NAME;
    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
    
    @Override
    CodeSandCmd getCmd(String userCodeParentPath, String userCodePath) {
        return CodeSandCmd
                .builder()
                .compileCmd(String.format("javac -encoding utf-8 %s", userCodePath))
                .runCmd(String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main", userCodeParentPath))
                .build();
    }
}
