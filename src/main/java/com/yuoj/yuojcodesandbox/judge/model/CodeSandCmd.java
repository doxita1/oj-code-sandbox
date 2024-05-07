package com.yuoj.yuojcodesandbox.judge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CodeSandCmd {
    private String runCmd;
    private String compileCmd;
}
