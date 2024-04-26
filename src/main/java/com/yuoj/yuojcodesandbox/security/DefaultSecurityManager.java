package com.yuoj.yuojcodesandbox.security;

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkRead(String file) {
        super.checkRead(file);
        
        if (file.contains("hutool")) {
            throw new SecurityException("Access Denied: You are not allowed to read files outside of safe directory.");
        }
    }
    
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("default manager"+ perm.getName());
        super.checkPermission(perm);
    }
}
