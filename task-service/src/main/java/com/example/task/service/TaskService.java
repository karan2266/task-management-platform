package com.example.task.service;

import com.example.task.client.UserValidationClient;
import com.example.task.dto.request.AssignTaskRequest;
import com.example.task.dto.request.CreateTaskRequest;
import com.example.task.dto.request.UpdateStatusRequest;
import com.example.task.dto.request.UpdateTaskRequest;
import com.example.task.dto.response.TaskResponse;
import com.example.task.entity.Project;
import com.example.task.entity.Task;
import com.example.task.enums.Priority;
import com.example.task.enums.TaskStatus;
import com.example.task.exception.InvalidStatusTransitionException;
import com.example.task.exception.ProjectNotFoundException;
import com.example.task.exception.TaskNotFoundException;
import com.example.task.repository.ProjectRepository;
import com.example.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserValidationClient userValidationClient;

    @Transactional
    public TaskResponse create(UUID projectId, CreateTaskRequest request, UUID requesterId,
                               String bearerToken) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        // If assigneeUserId is provided in the request, validate it exists and is active
        UUID assigneeId = request.getAssigneeUserId();
        if (assigneeId != null) {
            userValidationClient.getUser(assigneeId, bearerToken);
        }

        Task task = Task.builder()
                .project(project)
                .title(request.getTitle().strip())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .assigneeUserId(assigneeId)
                .dueDate(request.getDueDate())
                .build();

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> findByProject(UUID projectId, TaskStatus status) {
        if (!projectRepository.existsById(projectId)) {
            throw new ProjectNotFoundException(projectId);
        }
        List<Task> tasks = (status != null)
                ? taskRepository.findByProjectIdAndStatus(projectId, status)
                : taskRepository.findByProjectId(projectId);

        return tasks.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID projectId, UUID taskId) {
        Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException(taskId, projectId));
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks(UUID userId) {
        return taskRepository.findByAssigneeUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> getOverdueTasks() {
        return taskRepository.findByDueDateBeforeAndStatusNot(LocalDateTime.now(), TaskStatus.DONE)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse update(UUID projectId, UUID taskId, UpdateTaskRequest request) {
        Task task = getOrThrow(projectId, taskId);
        task.setTitle(request.getTitle().strip());
        task.setDescription(request.getDescription());
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        task.setDueDate(request.getDueDate());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse assign(UUID projectId, UUID taskId,
                               AssignTaskRequest request, String bearerToken) {
        Task task = getOrThrow(projectId, taskId);

        userValidationClient.getUser(request.getAssigneeUserId(), bearerToken);

        task.setAssigneeUserId(request.getAssigneeUserId());
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateStatus(UUID projectId, UUID taskId, UpdateStatusRequest request) {
        Task task = getOrThrow(projectId, taskId);
        TaskStatus current = task.getStatus();
        TaskStatus next = request.getStatus();

        if (!current.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }

        task.setStatus(next);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void delete(UUID projectId, UUID taskId) {
        Task task = getOrThrow(projectId, taskId);
        taskRepository.delete(task);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Task getOrThrow(UUID projectId, UUID taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new TaskNotFoundException(taskId, projectId));
    }

    TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .assigneeUserId(task.getAssigneeUserId())
                .dueDate(task.getDueDate())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}

