package com.example.task.controller;

import com.example.task.dto.request.CreateProjectRequest;
import com.example.task.dto.request.UpdateProjectRequest;
import com.example.task.dto.response.ProjectResponse;
import com.example.task.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /** Create a project — ADMIN only. ownerUserId is set from the JWT. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponse create(@Valid @RequestBody CreateProjectRequest request,
                                  Authentication auth) {
        UUID ownerId = (UUID) auth.getPrincipal();
        return projectService.create(request, ownerId);
    }

    /** List all projects. */
    @GetMapping
    public List<ProjectResponse> findAll() {
        return projectService.findAll();
    }

    /** Get a project by ID. */
    @GetMapping("/{id}")
    public ProjectResponse findById(@PathVariable UUID id) {
        return projectService.findById(id);
    }

    /** Update project name/description — ADMIN only. */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProjectResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody UpdateProjectRequest request) {
        return projectService.update(id, request);
    }

    /** Delete a project — ADMIN only. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable UUID id) {
        projectService.delete(id);
    }
}

