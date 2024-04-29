package com.yuoj.yuojcodesandbox.controller;


import com.yuoj.yuojcodesandbox.judge.JavaNativeCodeSandBox;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeRequest;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class ExecCodeController {
    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;
    
    
    @PostMapping("/executeCode")
    public ExecuteCodeResponse doExecCode(@RequestBody ExecuteCodeRequest executeCodeRequest){
        return javaNativeCodeSandBox.executeCode(executeCodeRequest);
    }
}
