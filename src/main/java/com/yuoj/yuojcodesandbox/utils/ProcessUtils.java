package com.yuoj.yuojcodesandbox.utils;

import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
                String compileReadLine;
                StringBuilder compileStringBuilder = new StringBuilder();
                while ((compileReadLine = bufferedReader.readLine()) != null) {
                    compileStringBuilder.append(compileReadLine).append("\n");
                }
                executeMessage.setSucceedMessage(String.valueOf(compileStringBuilder));
            } else {
                log.error("{}失败", type);
                System.out.println(String.format("%s失败", type));
                String compileReadLine;
                StringBuilder compileStringBuilder = new StringBuilder();
                while ((compileReadLine = bufferedReader.readLine()) != null) {
                    compileStringBuilder.append(compileReadLine).append("\n");
                }
                executeMessage.setSucceedMessage(String.valueOf(compileStringBuilder));
                
                
                BufferedReader bufferedReaderError = new BufferedReader
                        (new InputStreamReader(execProcess.getErrorStream()));
                String errorCompileReadLine;
                StringBuilder errorCompileStringBuilder = new StringBuilder();
                while ((errorCompileReadLine = bufferedReaderError.readLine()) != null) {
                    errorCompileStringBuilder.append(errorCompileReadLine).append("\n");
                }
                executeMessage.setErrorMessage(String.valueOf(errorCompileStringBuilder));
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        return executeMessage;
    }
}
