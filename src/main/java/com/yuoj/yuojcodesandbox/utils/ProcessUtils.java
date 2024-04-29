package com.yuoj.yuojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ProcessUtils {
    /**
     * 执行进程并获取执行信息
     * @param execProcess 进程
     * @param type 进程类别
     * @return
     */
    
    public static ExecuteMessage runProcessResult(Process execProcess,String type) {
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            int exitCode = execProcess.waitFor();
            executeMessage.setExitCode(exitCode);
            BufferedReader bufferedReader = new BufferedReader
                    (new InputStreamReader(execProcess.getInputStream()));
            
            if (exitCode == 0) {
                log.info("{}成功", type);
                System.out.println(String.format("%s成功", type));
                String compileReadLine;List<String> compileStringList = new ArrayList<>();
                while ((compileReadLine = bufferedReader.readLine()) != null) {
                    compileStringList.add(compileReadLine);
                }
                executeMessage.setSucceedMessage(StringUtils.join(compileStringList,"\n"));
            } else {
                log.error("{}失败", type);
                System.out.println(String.format("%s失败", type));
                String compileReadLine;
                List<String> compileStringList = new ArrayList<>();
                while ((compileReadLine = bufferedReader.readLine()) != null) {
                    compileStringList.add(compileReadLine);
                }
                
                executeMessage.setSucceedMessage(StringUtils.join(compileStringList,"\n"));
                BufferedReader bufferedReaderError = new BufferedReader
                        (new InputStreamReader(execProcess.getErrorStream()));
                String errorCompileReadLine;
                
                List<String> errorCompileStringList = new ArrayList<>();
                while ((errorCompileReadLine = bufferedReaderError.readLine()) != null) {
                    errorCompileStringList.add(errorCompileReadLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorCompileStringList,"\n"));
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        return executeMessage;
    }
}
