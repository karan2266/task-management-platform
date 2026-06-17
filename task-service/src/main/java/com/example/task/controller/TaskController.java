package com.example.task.controller;

import com.example.task.dto.request.AssignTaskRequest;
import com.example.task.dto.request.CreateTaskRequest;
import com.example.task.dto.request.UpdateStatusRequest;
import com.example.task.dto.request.UpdateTaskRequest;
import com.example.task.dto.response.TaskResponse;
import com.example.task.enums.TaskStatus;
import com.example.task.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /** Create a task inside a project. assigneeUserId is optional in the body. */
    @PostMapping("/api/v1/projects/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@PathVariable UUID projectId,
                               @Valid @RequestBody CreateTaskRequest request,
                               Authentication auth,
                               @RequestHeader("Authorization") String authHeader) {
        UUID requesterId = (UUID) auth.getPrincipal();
        String bearerToken = authHeader.substring(7);
        return taskService.create(projectId, request, requesterId, bearerToken);
    }

    /** List tasks for a project; optional ?status= filter. */
    @GetMapping("/api/v1/projects/{projectId}/tasks")
    public List<TaskResponse> findByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) TaskStatus status) {
        return taskService.findByProject(projectId, status);
    }

    /** Get a single task by ID within a project. */
    @GetMapping("/api/v1/projects/{projectId}/tasks/{taskId}")
    public TaskResponse findById(@PathVariable UUID projectId,
                                 @PathVariable UUID taskId) {
        return taskService.findById(projectId, taskId);
    }

    /** Update task title, description, and priority (full PUT). */
    @PutMapping("/api/v1/projects/{projectId}/tasks/{taskId}")
    public TaskResponse update(@PathVariable UUID projectId,
                               @PathVariable UUID taskId,
                               @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.update(projectId, taskId, request);
    }

    /** Delete a task — ADMIN only. */
    @DeleteMapping("/api/v1/projects/{projectId}/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID projectId,
                       @PathVariable UUID taskId) {
        taskService.delete(projectId, taskId);
    }

    /**
     * Assign (or re-assign) a task to a user — ADMIN only.
     * Validates the assignee exists and is active in auth-service before persisting.
     * Returns HTTP 400 if assigneeUserId is missing; HTTP 404 if user not found/inactive.
     */
    @PatchMapping("/api/v1/projects/{projectId}/tasks/{taskId}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponse assign(@PathVariable UUID projectId,
                               @PathVariable UUID taskId,
                               @Valid @RequestBody AssignTaskRequest request,
                               @RequestHeader("Authorization") String authHeader) {
        String bearerToken = authHeader.substring(7);
        return taskService.assign(projectId, taskId, request, bearerToken);
    }

    /**
     * Transition task status.
     * Valid transitions: TODO→IN_PROGRESS, IN_PROGRESS→DONE, DONE→IN_PROGRESS.
     * Any other transition returns HTTP 400.
     */
    @PatchMapping("/api/v1/projects/{projectId}/tasks/{taskId}/status")
    public TaskResponse updateStatus(@PathVariable UUID projectId,
                                     @PathVariable UUID taskId,
                                     @Valid @RequestBody UpdateStatusRequest request) {
        return taskService.updateStatus(projectId, taskId, request);
    }

    /** Get all tasks assigned to the currently authenticated user. */
    @GetMapping("/api/v1/tasks/my-tasks")
    public List<TaskResponse> getMyTasks(Authentication auth) {
        UUID userId = (UUID) auth.getPrincipal();
        return taskService.getMyTasks(userId);
    }

    /** Get all overdue tasks across all projects. */
    @GetMapping("/api/v1/tasks/overdue")
    public List<TaskResponse> getOverdueTasks() {
        return taskService.getOverdueTasks();
    }
}

