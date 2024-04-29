package com.yuoj.yuojcodesandbox.judge.model;

import lombok.Data;

@Data
public class JudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;
    
    /**
     * 执行时间
     */
    private Long time;
    /**
     * 消耗内存
     */
    private Long memory;

}
