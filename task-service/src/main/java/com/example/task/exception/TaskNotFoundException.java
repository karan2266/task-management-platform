package com.example.task.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(UUID taskId) {
        super("Task not found with id: " + taskId);
    }

    public TaskNotFoundException(UUID taskId, UUID projectId) {
        super("Task " + taskId + " not found in project " + projectId);
    }
}

