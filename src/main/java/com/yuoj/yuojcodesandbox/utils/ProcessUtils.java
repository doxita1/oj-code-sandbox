package com.yuoj.yuojcodesandbox.utils;

import cn.hutool.core.date.StopWatch;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
public class ProcessUtils {
    /**
     * 执行进程并获取执行信息
     * @param execProcess 进程
     * @param type 进程类别
     * @return
     */
    public static final StopWatch stopWatch = new StopWatch();
    public static final long RUN_TIME_LIMIT = 3 * 1000L;
    
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
                List<String> compileStringList = new ArrayList<>();
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
    
    public static List<ExecuteMessage> executeCppCode(List<String> inputList, String executablePath) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            ProcessBuilder pb = new ProcessBuilder(executablePath.split(" "));
            pb.redirectErrorStream(true); // 将错误输出和标准输出合并
            ExecuteMessage executeMessage = new ExecuteMessage();
            try {
                Process process = pb.start();
                // 通过标准输入流写入输入数据
                try {
                    stopWatch.start();
                    // 超时控制, 开启子线程来计时
                    new Thread(() -> {
                        try {
                            Thread.sleep(RUN_TIME_LIMIT);
                            if (process.isAlive()) {
                                System.out.println("run time out of limit");
                                process.destroy();
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }).start();
                    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                        writer.write(input.replace(" ", "\n"));
                    }
                    
                    // 读取标准输出
                    List<String> outputLines = readLines(process.getInputStream());
                    outputLines.forEach(System.out::println); // 或其他逻辑处理
                    executeMessage.setSucceedMessage(String.join("", outputLines));
                    
                    // 读取错误输出
                    List<String> errorLines = readLines(process.getErrorStream());
                    errorLines.forEach(System.err::println); // 或其他逻辑处理
                    executeMessage.setErrorMessage(String.join("", errorLines));
                    
                    int exitCode = process.waitFor();
                    executeMessage.setExitCode(exitCode);
                    stopWatch.stop();
                    // 得到运行时间
                    executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
                    executeMessageList.add(executeMessage);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return executeMessageList;
    }
    
    private static List<String> readLines(InputStream inputStream) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }
    
}
