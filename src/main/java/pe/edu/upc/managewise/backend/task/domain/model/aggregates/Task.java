package pe.edu.upc.managewise.backend.task.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import pe.edu.upc.managewise.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
public class Task extends AuditableAbstractAggregateRoot<Task> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> assignedUsers;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    protected Task() {
        super();
        this.assignedUsers = new HashSet<>();
    }

    public Task(Project project, String title, String description, Date startDate, Date endDate, Status status, Priority priority) {
        this();
        this.project = project;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.priority = priority;
    }

    public void assignUser(User user) {
        this.assignedUsers.add(user);
    }

    public void removeUser(User user) {
        this.assignedUsers.remove(user);
    }

    public void updateStatus(Status newStatus) {
        this.status = newStatus;
    }

    public void updateDetails(String title, String description, Date endDate, Priority priority) {
        this.title = title;
        this.description = description;
        this.endDate = endDate;
        this.priority = priority;
    }
}
