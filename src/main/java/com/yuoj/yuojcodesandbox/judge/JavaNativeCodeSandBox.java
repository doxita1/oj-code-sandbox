package com.yuoj.yuojcodesandbox.judge;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeRequest;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeResponse;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import com.yuoj.yuojcodesandbox.judge.model.JudgeInfo;
import com.yuoj.yuojcodesandbox.security.DefaultSecurityManager;
import com.yuoj.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
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
public class JavaNativeCodeSandBox implements CodeSandBox {
    
    public static final String GLOBAL_FILE_NAME = "tempCode";
    
    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    
    public static final Long RUN_TIME_LIMIT = 1000 * 3L;
    
    public static final List<String> BLACK_LIST = Arrays.asList("exec","file");
    
    public static final WordTree WORD_TREE;
    
    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACK_LIST);
    }
    
    public static void main(String[] args) throws IOException, InterruptedException {
        
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        
        System.setSecurityManager(new DefaultSecurityManager());
        
//        String code = FileUtil.readString(FileUtil.newFile("D:\\yupi-oj\\yuoj-code-sandbox\\src\\main\\resources\\testJavaCode\\simpleAdd.java"), CharsetUtil.CHARSET_UTF_8);
        String code = FileUtil.readString(FileUtil.newFile("D:\\yupi-oj\\yuoj-code-sandbox\\src\\main\\resources\\errorstatus\\TimeOutOfLimit.java"), CharsetUtil.CHARSET_UTF_8);
//        String code = FileUtil.readString(FileUtil.newFile("D:\\yupi-oj\\yuoj-code-sandbox\\src\\main\\resources\\errorstatus\\OutOfMemory.java"),CharsetUtil.CHARSET_UTF_8);
//        String code = FileUtil.readString(FileUtil.newFile("D:\\yupi-oj\\yuoj-code-sandbox\\src\\main\\resources\\errorstatus\\createFile.java"),CharsetUtil.CHARSET_UTF_8);
        
        // 匹配违禁词
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord!=null) {
            System.out.println("有违禁词:"+foundWord.getFoundWord());
        }
        
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setInputList(Arrays.asList("16 2", "3 4"));
        executeCodeRequest.setCode(code);
        
        javaNativeCodeSandBox.executeCode(executeCodeRequest);
        
    }
    
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        List<String> inputList = executeCodeRequest.getInputList();
        
        
        //1.把用户的代码保存为文件
        String property = System.getProperty("user.dir");
        //创键临时代码文件
        String tempCodePath = property + FileUtil.FILE_SEPARATOR + GLOBAL_FILE_NAME;
        
        if (!FileUtil.exist(tempCodePath)) {
            FileUtil.mkdir(tempCodePath);
        }
        
        String userCodeParentPath = tempCodePath + FileUtil.FILE_SEPARATOR + UUID.randomUUID();
        String userCodePath = userCodeParentPath + FileUtil.FILE_SEPARATOR + GLOBAL_JAVA_CLASS_NAME;
        String securityPath = "D:\\yupi-oj\\yuoj-code-sandbox\\src\\main\\resources\\security\\DefaultSecurityManager.java";
        File userCodeFile = FileUtil.writeString(code, userCodePath, CharsetUtil.CHARSET_UTF_8);
        
        
        
        //2.编译代码,得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessResult(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (IOException e) {
            return getErrorResponse(e);
        }
        // 用stopwatch计时
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();
        
        //3.执行代码,得到输出结果
        for (int i = 0; i < inputList.size(); i++) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputList.get(i));
            try {
                stopWatch.start();
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 超时控制, 开启子线程来计时
                new Thread(() -> {
                    try {
                        Thread.sleep(RUN_TIME_LIMIT);
                        System.out.println("run time out of limit");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage runExecuteMessage = ProcessUtils.runProcessResult(runProcess, "运行");
                stopWatch.stop();
                // 得到运行时间
                runExecuteMessage.setTime(stopWatch.getLastTaskTimeMillis());
                executeMessageList.add(runExecuteMessage);
            } catch (IOException e) {
                return getErrorResponse(e);
            }
        }
        //4.收集整理输出结果
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
//        judgeInfo.setMessage();
        judgeInfo.setTime(maxTime);
//        judgeInfo.setMemory(); // 非常麻烦, 不做
        
        executeCodeResponse.setJudgeInfo(judgeInfo);
        System.out.println(executeCodeResponse);
        
        //5.文件清理,释放空间
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile());
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        
        //6.错误处理,提升程序健壮性
        
        return executeCodeResponse;
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
