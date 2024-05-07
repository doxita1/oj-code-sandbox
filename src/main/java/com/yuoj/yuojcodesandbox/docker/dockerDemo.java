package com.yuoj.yuojcodesandbox.docker;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.core.DockerClientBuilder;

public class dockerDemo {
    public static void main(String[] args) throws InterruptedException {
        DockerClient client = DockerClientBuilder.getInstance().build();
        String imageName = "redis:latest";
        PullImageCmd pullImageCmd = client.pullImageCmd(imageName);
        PullImageResultCallback resultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.err.println("message:" + item.getStatus());
                super.onNext(item);
            }
        };
        pullImageCmd.exec(resultCallback)
                .awaitCompletion(); // 等待结束
    }
}
