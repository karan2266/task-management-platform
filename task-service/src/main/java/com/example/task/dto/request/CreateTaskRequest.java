package com.example.task.dto.request;

import com.example.task.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private Priority priority;

    private UUID assigneeUserId;

    private LocalDateTime dueDate;
}

