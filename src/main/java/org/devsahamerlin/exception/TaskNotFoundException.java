package org.devsahamerlin.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String massage) {
        super(massage);
    }
}

