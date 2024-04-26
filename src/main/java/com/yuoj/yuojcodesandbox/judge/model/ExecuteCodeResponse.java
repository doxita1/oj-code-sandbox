package com.yuoj.yuojcodesandbox.judge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteCodeResponse {
    private String message;
    private JudgeInfo judgeInfo;
    private Integer status;
    private List<String> outputList;
}
