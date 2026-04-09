package pe.edu.upc.managewise.backend.project.domain.services;

import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.project.domain.model.commands.*;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectCode;

import java.util.Optional;

public interface ProjectCommandService {
    Long handle(CreateProjectCommand createProjectCommand);
    Optional<Project> handle(UpdateProjectCommand updateProjectCommand);
    void handle(DeleteProjectCommand deleteProjectCommand);
    Optional<Project> handle(AddUserToProjectCommand addUserToProjectCommand);
    void handle(RemoveUserFromProjectCommand removeUserFromProjectCommand, Long leaderId);
    Optional<Project> handle(ResetCodeCommand resetCodeCommand);
    Optional<ProjectCode> handle(SetCodeCommand setCodeCommand);
}
