package org.seaPack.exception;

/**
 * 循环依赖异常
 */
public class CyclicDependencyException extends RuntimeException{
    public CyclicDependencyException(String message) {
        super(message);
    }
}
