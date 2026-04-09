package pe.edu.upc.managewise.backend.project.domain.services;

import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.project.domain.model.queries.*;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectCode;

import java.util.List;
import java.util.Optional;

public interface ProjectQueryService {
    List<Project> handle(GetAllProjectsQuery getAllProjectsQuery);
    Optional<Project> handle(GetProjectByIdQuery getProjectByIdQuery);
    Optional<ProjectCode> handle(GetProjectCodeByIdQuery getProjectCodeByIdQuery);
    List<Project> handle(GetProjectsByLeaderIdQuery getProjectsByLeaderIdQuery);
    List<Project> handle(GetProjectsByMemberIdQuery getProjectsByMemberIdQuery);
}