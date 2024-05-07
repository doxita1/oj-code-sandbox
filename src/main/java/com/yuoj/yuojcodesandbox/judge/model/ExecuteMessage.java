package com.yuoj.yuojcodesandbox.judge.model;

import lombok.Data;

@Data
public class ExecuteMessage {
    /**
     * 返回码
     */
    private Integer exitCode;
    /**
     * 成功信息
     */
    private String succeedMessage;
    /**
     * 错误信息
     */
    private String errorMessage;
    /**
     * 消耗时间
     */
    private Long time;
    
    private Long memory;
}
