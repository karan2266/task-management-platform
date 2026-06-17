package com.example.task.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignTaskRequest {

    @NotNull(message = "Assignee user ID is required")
    private UUID assigneeUserId;
}

