package com.example.task.service;

import com.example.task.dto.request.CreateProjectRequest;
import com.example.task.dto.request.UpdateProjectRequest;
import com.example.task.dto.response.ProjectResponse;
import com.example.task.entity.Project;
import com.example.task.exception.ProjectNotFoundException;
import com.example.task.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectResponse create(CreateProjectRequest request, UUID ownerId) {
        Project project = Project.builder()
                .name(request.getName().strip())
                .description(request.getDescription())
                .ownerUserId(ownerId)
                .build();
        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll() {
        return projectRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(UUID id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ProjectResponse update(UUID id, UpdateProjectRequest request) {
        Project project = getOrThrow(id);
        project.setName(request.getName().strip());
        project.setDescription(request.getDescription());
        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ProjectNotFoundException(id);
        }
        projectRepository.deleteById(id);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Project getOrThrow(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerUserId(project.getOwnerUserId())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}

