public class DefaultSecurityManager extends SecurityManager{
    @Override
    public void checkRead(String file) {
        super.checkRead(file);
        
        if (file.contains("hutool")) {
            throw new SecurityException("Access Denied: You are not allowed to read files outside of safe directory.");
        }
    }
}