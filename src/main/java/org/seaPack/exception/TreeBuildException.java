package org.seaPack.exception;

/**
 * 树构建异常
 */
public class TreeBuildException extends RuntimeException {
    public TreeBuildException(String message) {
        super(message);
    }

    public TreeBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}
