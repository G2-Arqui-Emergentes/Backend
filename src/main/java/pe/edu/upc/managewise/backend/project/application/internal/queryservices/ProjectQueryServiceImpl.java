package pe.edu.upc.managewise.backend.project.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.managewise.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.project.domain.model.queries.*;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectCode;
import pe.edu.upc.managewise.backend.project.domain.services.ProjectQueryService;
import pe.edu.upc.managewise.backend.project.infrastructure.persistence.jpa.repositories.ProjectRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectQueryServiceImpl implements ProjectQueryService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectQueryServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Project> handle(GetAllProjectsQuery query) {
        return this.projectRepository.findAll();
    }

    @Override
    public Optional<Project> handle(GetProjectByIdQuery query) {
        return this.projectRepository.findById(query.projectId());
    }

    @Override
    public Optional<ProjectCode> handle(GetProjectCodeByIdQuery getProjectCodeByIdQuery) {
        var optionalProject = projectRepository.findById(getProjectCodeByIdQuery.projectId());

        if (optionalProject.isEmpty()){
            throw new IllegalArgumentException("Project not found");
        }

        var projectCode = optionalProject.get().getProjectCode();

        return Optional.of(projectCode);
    }

    @Override
    public List<Project> handle(GetProjectsByMemberIdQuery getProjectsByMemberIdQuery) {
        var optionalMember= userRepository.findById(getProjectsByMemberIdQuery.memberId());
        if(optionalMember.isEmpty()){
            throw new IllegalArgumentException("Member not found");
        }
        if (optionalMember.get().getRoles().stream().noneMatch(role -> role.getName().equals(Roles.ROLE_MEMBER))) {
            throw new IllegalArgumentException("User is not a member");
        }
        var member = optionalMember.get();
        var optionalProjectList= member.getMemberInProjects().stream().toList();
        if(optionalProjectList.isEmpty()){
            throw new IllegalArgumentException("Member is not assigned to any project");
        }
        return optionalProjectList;
    }

    public List<Project> handle(GetProjectsByLeaderIdQuery getProjectsByLeaderIdQuery) {
        return projectRepository.findByLeaderId(getProjectsByLeaderIdQuery.leaderId());
    }
}
