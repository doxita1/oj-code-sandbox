package com.yuoj.yuojcodesandbox.judge;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.yuoj.yuojcodesandbox.judge.model.*;
import com.yuoj.yuojcodesandbox.utils.ProcessUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class CodeSandBoxTemplate implements CodeSandBox {
    
    public String GLOBAL_FILE_NAME;
    public String GLOBAL_LANGUAGE_NAME;
    public Long RUN_TIME_LIMIT = 1000 * 3L;
    
//    public static final List<String> BLACK_LIST = Arrays.asList("exec", "file");

//    public static final WordTree WORD_TREE;
    
    public static final StopWatch stopWatch = new StopWatch();
    
//
//    static {
//        WORD_TREE = new WordTree();
//        WORD_TREE.addWords(BLACK_LIST);
//    }
    
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        try {
            //1.把用户的代码保存为文件
            File userCodeFile = saveCodeToFile(code);
            
            CodeSandCmd cmd = getCmd(userCodeFile.getParentFile().getAbsolutePath(), userCodeFile.getAbsolutePath());
            //2.编译代码,得到class文件
            compileFile(userCodeFile,cmd.getCompileCmd());
            //3.执行代码,得到输出结果
            List<ExecuteMessage> executeMessageList = runCodeFile(userCodeFile, inputList,cmd.getRunCmd());
            
            //4.收集整理输出结果
            executeCodeResponse = getOutputResponse(executeMessageList);
            //5.文件清理,释放空间
//            boolean success = cleanFile(userCodeFile);
//            if(!success){
//                throw new RuntimeException("删除文件失败");
//            }
        }catch (Exception e){
            return getErrorResponse(e);
        }
        //6.错误处理,提升程序健壮性
        return executeCodeResponse;
    }
    
    abstract CodeSandCmd getCmd(String userCodeParentPath, String userCodePath);
    
    /**
     * //1.把用户的代码保存为文件
     *
     * @param code
     * @return
     */
    public File saveCodeToFile(String code) {
        
        //创键临时代码文件
        String tempCodePath = GLOBAL_FILE_NAME;
        
        if (!FileUtil.exist(tempCodePath)) {
            FileUtil.mkdir(tempCodePath);
        }
        String userCodeParentPath = tempCodePath + FileUtil.FILE_SEPARATOR + UUID.randomUUID();
        String userCodePath = userCodeParentPath + FileUtil.FILE_SEPARATOR + GLOBAL_LANGUAGE_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, CharsetUtil.CHARSET_UTF_8);
        return userCodeFile;
    }
    
    /**
     *
     //2.编译代码,得到class文件
     * @param userCodeFile
     */
    public void compileFile(File userCodeFile, String compileCmd) {
//        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessResult(compileProcess, "编译");
            System.out.println(executeMessage);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * //3.执行代码,得到输出结果
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runCodeFile(File userCodeFile,List<String > inputList,String runCmd) {
        
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (int i = 0; i < inputList.size(); i++) {
//            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputList.get(i));
            String tempCmd ="";
            if(userCodeFile.getAbsolutePath().endsWith("java")){
                tempCmd =runCmd + " "+ inputList.get(i);
            }
            try {
                stopWatch.start();
                Process runProcess = Runtime.getRuntime().exec(tempCmd);
                // 超时控制, 开启子线程来计时
                new Thread(() -> {
                    try {
                        Thread.sleep(RUN_TIME_LIMIT);
                        if (runProcess.isAlive()) {
                            System.out.println("run time out of limit");
                            runProcess.destroy();
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
//                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage runExecuteMessage = ProcessUtils.runProcessResult(runProcess, "运行");
                stopWatch.stop();
                // 得到运行时间
                runExecuteMessage.setTime(stopWatch.getLastTaskTimeMillis());
                executeMessageList.add(runExecuteMessage);
            } catch (IOException e) {
                return null;
            }
        }
        return executeMessageList;
    }
    
    /**
     *
     //4.收集整理输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0L;//记录最大运行时间来判断是否超时
        for (ExecuteMessage executeMessage : executeMessageList) {
            
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                //todo 定义状态枚举值
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getSucceedMessage());
            maxTime = Math.max(maxTime, executeMessage.getTime());
        }
        if (outputList.size() == executeMessageList.size()) {
            //没有错误输出
            executeCodeResponse.setMessage("运行成功");
            executeCodeResponse.setOutputList(outputList);
            executeCodeResponse.setStatus(1);
        }
        
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("运行成功");
        judgeInfo.setTime(maxTime);
        judgeInfo.setMemory(0L); //默认0
        executeCodeResponse.setJudgeInfo(judgeInfo);
        System.out.println(executeCodeResponse);
        return executeCodeResponse;
    }
    
    /**
     * 删除临时文件
     * @param userCodeFile
     * @return
     */
    public boolean cleanFile(File userCodeFile){
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return false;
    }
    

    public ExecuteCodeResponse getErrorResponse(Throwable e) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setMessage(e.getMessage());
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        executeCodeResponse.setStatus(2);// 表示代码沙箱错误, 如编译错误
        executeCodeResponse.setOutputList(new ArrayList<>());
        return executeCodeResponse;
    }
}

