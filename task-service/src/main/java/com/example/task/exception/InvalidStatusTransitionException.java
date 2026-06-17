package com.example.task.exception;

import com.example.task.enums.TaskStatus;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(TaskStatus from, TaskStatus to) {
        super("Invalid status transition from " + from + " to " + to
                + ". Allowed transitions from " + from + ": " + from.getAllowedTransitions());
    }
}

