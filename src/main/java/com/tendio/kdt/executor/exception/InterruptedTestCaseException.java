package com.tendio.kdt.executor.exception;

public class InterruptedTestCaseException extends RuntimeException {
    public InterruptedTestCaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptedTestCaseException(Throwable cause) {
        super(cause);
    }

    public InterruptedTestCaseException(String message) {
        super(message);
    }
}
