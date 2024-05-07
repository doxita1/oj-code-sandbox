package com.yuoj.yuojcodesandbox.controller;


import com.yuoj.yuojcodesandbox.judge.CPPNativeCodeSandBox;
import com.yuoj.yuojcodesandbox.judge.CodeSandBoxTemplate;
import com.yuoj.yuojcodesandbox.judge.JavaNativeCodeSandBox;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeRequest;
import com.yuoj.yuojcodesandbox.judge.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class ExecCodeController {
    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;
    
    @Resource
    private CPPNativeCodeSandBox cppNativeCodeSandBox;
    
    
    @PostMapping("/executeCode")
    public ExecuteCodeResponse doExecCode(@RequestBody ExecuteCodeRequest executeCodeRequest){
      
        String language = executeCodeRequest.getLanguage();
        if(language.equals("java")){
            return javaNativeCodeSandBox.executeCode(executeCodeRequest);
        }else{
            return cppNativeCodeSandBox.executeCode(executeCodeRequest);
        }
    }
}
