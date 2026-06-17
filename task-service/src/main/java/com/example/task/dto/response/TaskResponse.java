package com.example.task.dto.response;

import com.example.task.enums.Priority;
import com.example.task.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private UUID id;
    private UUID projectId;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private UUID assigneeUserId;
    private Instant createdAt;
    private Instant updatedAt;
}

