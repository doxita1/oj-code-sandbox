package com.yuoj.yuojcodesandbox.judge;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeRequest;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeResponse;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteMessage;
import com.yuoj.yuojcodesandbox.judge.model.JudgeInfo;
import com.yuoj.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MyJavaDockerCodeSandBox implements CodeSandBox {

    public static final String GLOBAL_FILE_NAME = "tempCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";

    public static final Long RUN_TIME_LIMIT = 1000 * 3L;

    public static final List<String> BLACK_LIST = Arrays.asList("exec", "file");

    public static final WordTree WORD_TREE;

    public static final boolean[] FIRST_INIT = {true};

    static {
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(BLACK_LIST);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println(System.getProperty("user.dir"));
        MyJavaDockerCodeSandBox javaNativeCodeSandBox = new MyJavaDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();

//        String code = FileUtil.readString(FileUtil.newFile("D:\\yupi-oj\\yuoj-code-sandbox\\src\\main\\resources\\testJavaCode\\simpleAdd.java"), CharsetUtil.CHARSET_UTF_8);
        String code = FileUtil.readString(FileUtil.newFile("/home/doxita/code/src/main/resources/testJavaCode/simpleAdd.java"), CharsetUtil.CHARSET_UTF_8);
        executeCodeRequest.setLanguage("java");
        executeCodeRequest.setInputList(Arrays.asList("16 2", "3 4","1 2"));
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

        // 3.创键docker容器,上传编译文件
        //3.1拉取java镜像
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String openJavaImage = "openjdk:8-alpine";
        // 只有第一次拉取

        if (FIRST_INIT[0]) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(openJavaImage);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.err.println(item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        .awaitCompletion();
                FIRST_INIT[0] = false;
            } catch (InterruptedException e) {
                log.error("拉取镜像失败");
                throw new RuntimeException(e);
            }
        }
        System.out.println("下载完成");
        //创键容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(openJavaImage);
        HostConfig hostConfig = new HostConfig();

        hostConfig.withMemory(100 * 1000 * 1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        // 安全设置

//        hostConfig.withSecurityOpts(Arrays.asList("seccomp=/home/doxita/code/src/main/resources/default.json"));

        hostConfig.setBinds(new Bind(userCodeParentPath , new Volume("/app"))); // 挂载数据卷

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // docker exec keen_blackwell java -cp /app Main 1 3
        // 执行命令并获取结果
        // 用stopwatch计时
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        StopWatch stopWatch = new StopWatch();

        final long[] maxMemory = {0L};
        for (String inputArgs : inputList) {
            String[] inputArg = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArg);

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)
                    .withAttachStdin(true)
                    .withAttachStdout(true)
                    .exec();
            System.out.println("创建执行命令：" + execCreateCmdResponse);

            ExecuteMessage executeMessage = new ExecuteMessage();
            String execId = execCreateCmdResponse.getId();
            // 获取内存占用
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = new ResultCallback<Statistics>() {
                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            };
            statsCmd.exec(statisticsResultCallback);

            final boolean[] timeout = {true};
            final String[] message = {null};
            final String[] errorMessage = {null};
            long time = 0L;
            ExecStartResultCallback frameResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    // 如果执行完成，则表示没超时
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }
            };
            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(frameResultCallback)
                        .awaitCompletion(RUN_TIME_LIMIT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            executeMessage.setSucceedMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }

        // 执行完后删除容器
        dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        // 4、封装结果，跟原生实现方式完全一致
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 取用时最大值，便于判断是否超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                // 用户提交的代码执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getSucceedMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        // 正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setMessage("执行成功");
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(maxMemory[0]);
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
//        5. 文件清理
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        System.out.println(executeCodeResponse);
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
