package dev.jvmai.diagnostic;

public class JvmAttachException extends RuntimeException {
    public JvmAttachException(String message, Throwable cause) {
        super(message, cause);
    }

    public JvmAttachException(String message) {
        super(message);
    }
}
