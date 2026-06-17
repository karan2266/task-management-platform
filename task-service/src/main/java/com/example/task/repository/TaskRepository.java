package com.example.task.repository;

import com.example.task.entity.Task;
import com.example.task.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    /** All tasks belonging to a specific project (used for list + status-filter). */
    List<Task> findByProjectId(UUID projectId);

    /** Tasks filtered by project AND status (supports ?status= query param). */
    List<Task> findByProjectIdAndStatus(UUID projectId, TaskStatus status);

    /** All tasks assigned to a specific user (used for GET /api/v1/tasks/my-tasks). */
    List<Task> findByAssigneeUserId(UUID assigneeUserId);

    /** Single task scoped to a project — prevents cross-project task access. */
    Optional<Task> findByIdAndProjectId(UUID id, UUID projectId);
}

