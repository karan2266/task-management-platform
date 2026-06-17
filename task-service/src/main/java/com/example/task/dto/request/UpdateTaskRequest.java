package com.example.task.dto.request;

import com.example.task.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    private Priority priority;

    private LocalDateTime dueDate;
}

