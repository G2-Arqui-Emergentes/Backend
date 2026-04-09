package pe.edu.upc.managewise.backend.project.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.managewise.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.project.domain.model.commands.*;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectCode;
import pe.edu.upc.managewise.backend.project.domain.services.ProjectCommandService;
import pe.edu.upc.managewise.backend.project.infrastructure.persistence.jpa.repositories.ProjectRepository;
import pe.edu.upc.managewise.backend.notification.domain.services.NotificationCommandService;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.CreateNotificationCommand;

import java.util.Date;
import java.util.Optional;
@Service
public class ProjectCommandServiceImpl implements ProjectCommandService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationCommandService notificationCommandService;

    public ProjectCommandServiceImpl(ProjectRepository projectRepository, UserRepository userRepository,
                                     NotificationCommandService notificationCommandService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationCommandService = notificationCommandService;
    }

    @Override
    public Long handle(CreateProjectCommand createProjectCommand) {
        var leaderOptional = userRepository.findById(createProjectCommand.leaderId());
        if (leaderOptional.isEmpty()) {
            throw new IllegalArgumentException("Leader with ID " + createProjectCommand.leaderId() + " not found");
        }

        var leader = leaderOptional.get();

        boolean isleader = leader.getRoles().stream()
                .anyMatch(role -> role.getName() == Roles.ROLE_LEADER);

        if (!isleader) {
            throw new IllegalArgumentException("Only leaders can create projects");
        }

        var project=new Project(createProjectCommand);
        projectRepository.save(project);

        return project.getId();
    }

    @Override
    public Optional<Project> handle(UpdateProjectCommand updateProjectCommand) {
        var optionalProject=projectRepository.findById(updateProjectCommand.projectId());

        if (optionalProject.isEmpty()) {
            throw new IllegalArgumentException("Project with ID " + updateProjectCommand.projectId() + " not found");
        }

        var project=optionalProject.get();

        var updatedProject=project.updateProject(updateProjectCommand);

        projectRepository.save(updatedProject);

        return  Optional.of(updatedProject);
    }

    @Override
    public void handle(DeleteProjectCommand deleteProjectCommand) {
        if (!projectRepository.existsById(deleteProjectCommand.projectId())) {
            throw new IllegalArgumentException("Project with ID " + deleteProjectCommand.projectId() + " not found");
        }
        try{
            var allUsers = userRepository.findAll();

            allUsers.forEach(user -> {
                user.removeFromProject(deleteProjectCommand.projectId());
                userRepository.save(user);
            });

            projectRepository.deleteById(deleteProjectCommand.projectId());

        } catch (Exception e){
            throw new RuntimeException("Error while deleting project",e);
        }
    }

    @Override
    public Optional<Project> handle(AddUserToProjectCommand addUserToProjectCommand) {
        var projectOptional = projectRepository.findAll().stream()
                .filter(project -> project.getProjectCode() != null
                        && project.getProjectCode().key().equals(addUserToProjectCommand.code()))
                .findFirst();

        if (projectOptional.isEmpty()) {
            throw new IllegalArgumentException("Project with code " + addUserToProjectCommand.code() + " not found");
        }

        var project = projectOptional.get();
        if (project.getProjectCode().expiration().before(new Date())) {
            throw new IllegalArgumentException("Code " + addUserToProjectCommand.code() + " has expired");
        }

        var userOptional = userRepository.findById(addUserToProjectCommand.memberId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + addUserToProjectCommand.memberId() + " not found");
        }

        var user = userOptional.get();

        boolean alreadyInProject = user.getMemberInProjects().stream()
                .anyMatch(p -> p.getId().equals(project.getId()));

        if (!alreadyInProject) {
            user.assignToProject(project);
            userRepository.save(user);


            try {
                Long leaderId = project.getLeaderId();
                String title = "Nuevo miembro en el proyecto";
                String message = String.format("El usuario %s %s se unió al proyecto %s", user.getName(), user.getLastName(), project.getName());
                System.out.println("[Notification] Enviando notificación al líderId=" + leaderId + ", mensaje=" + message);
                notificationCommandService.handle(new CreateNotificationCommand(leaderId, title, message));
            } catch (Exception ignored) {

            }
        }

        return Optional.of(project);
    }

    @Override
    public void handle(RemoveUserFromProjectCommand removeUserFromProjectCommand, Long leaderId) {

        var optionalProject=projectRepository.findById(removeUserFromProjectCommand.projectId());

        if (optionalProject.isEmpty()) {
            throw new IllegalArgumentException("Project with ID " + removeUserFromProjectCommand.projectId() + " not found");
        }

        var project=optionalProject.get();

        var optionalUser=userRepository.findById(removeUserFromProjectCommand.memberId());

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("Project with ID " + removeUserFromProjectCommand.memberId() + " not found");
        }

        var member=optionalUser.get();

        var optionalLeader=userRepository.findById(leaderId);

        if (optionalLeader.isEmpty()) {
            throw new IllegalArgumentException("Leader with ID " + leaderId + " not found");
        }

        var leader=optionalLeader.get();

        boolean isOwner= project.getLeaderId().equals(leaderId);

        if (!isOwner) {
            throw new IllegalArgumentException("Leader with ID " + leaderId + " is not the owner of this project");
        }

        boolean isAssigned = member.getMemberInProjects().stream().anyMatch(p->p.getId().equals(project.getId()));

        if (!isAssigned) {
            throw new IllegalArgumentException("Member with ID " + member.getId() + " is not assigned in this project");
        }

        member.removeFromProject(removeUserFromProjectCommand.projectId());

        userRepository.save(member);
    }

    @Override
    public Optional<Project> handle(ResetCodeCommand resetCodeCommand) {
        var optionalProject=projectRepository.findById(resetCodeCommand.projectId());

        if (optionalProject.isEmpty()) {
            throw new IllegalArgumentException("Project with ID " + resetCodeCommand.projectId() + " not found");
        }

        var project=optionalProject.get();

        project.resetCode();

        projectRepository.save(project);

        return  Optional.of(project);
    }

    @Override
    public Optional<ProjectCode> handle(SetCodeCommand setCodeCommand) {
        var optionalProject=projectRepository.findById(setCodeCommand.projectId());

        if (optionalProject.isEmpty()) {
            throw new IllegalArgumentException("Project with ID " + setCodeCommand.projectId() + " not found");
        }

        var project=optionalProject.get();

        var isAssigned = projectRepository.findAll().stream().anyMatch(p->p.getProjectCode().key().equals(setCodeCommand.keycode()));

        if (isAssigned) {
            throw new IllegalArgumentException("Project with ID " + setCodeCommand.projectId() + " is already assigned to any project");
        }

        var updatedProjectCode=new ProjectCode(setCodeCommand.keycode(),setCodeCommand.expiration());
        var updatedProject=project.setCode(updatedProjectCode);

        projectRepository.save(updatedProject);

        return Optional.of(updatedProjectCode);
    }
}
