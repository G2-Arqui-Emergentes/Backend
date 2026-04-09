package pe.edu.upc.managewise.backend.project.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import pe.edu.upc.managewise.backend.project.domain.model.commands.CreateProjectCommand;
import pe.edu.upc.managewise.backend.project.domain.model.commands.UpdateProjectCommand;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectCode;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectStatus;
import pe.edu.upc.managewise.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;

import java.util.*;

@Getter
@Entity
public class Project extends AuditableAbstractAggregateRoot<Project> {

    @Embedded
    private ProjectCode projectCode;

    private Long leaderId;
    private String name;
    private String description;
    private String imageUrl;
    private Double budget;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    private Date startDate;
    private Date endDate;

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();

    public Project() {
    }

    public Project(CreateProjectCommand createProjectCommand) {
        this.projectCode = generateProjectCode();
        this.leaderId = createProjectCommand.leaderId();
        this.name = createProjectCommand.name();
        this.description = createProjectCommand.description();
        this.imageUrl = createProjectCommand.imageUrl();
        this.budget = createProjectCommand.budget();
        this.status = ProjectStatus.PLANNED;
        this.startDate = new Date();
        this.endDate = createProjectCommand.endDate();
    }

    public Project updateProject(UpdateProjectCommand updateProjectCommand) {
        this.name = updateProjectCommand.name();
        this.description = updateProjectCommand.description();
        this.imageUrl = updateProjectCommand.imageUrl();
        this.budget = updateProjectCommand.budget();
        this.status = updateProjectCommand.status();
        this.endDate = updateProjectCommand.endDate();
        return this;
    }

    public void changeStatus(ProjectStatus status) {
        this.status = status;
    }

    public Project setCode(ProjectCode projectCode) {
        this.projectCode = projectCode;
        return this;
    }

    public Project resetCode() {
        this.projectCode = null;
        return this;
    }

    private ProjectCode generateProjectCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int randomIndex = random.nextInt(characters.length());
            key.append(characters.charAt(randomIndex));
        }
        Date expiration = java.sql.Timestamp.valueOf(
                java.time.LocalDateTime.now().plusMonths(6)
        );
        return new ProjectCode(key.toString(), expiration);
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }

}
