package pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.taskmaster.backend.iam.domain.model.entities.Role;
import pe.edu.upc.taskmaster.backend.iam.domain.model.valueobjects.UserStatus;
import pe.edu.upc.taskmaster.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.taskmaster.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class User extends AuditableAbstractAggregateRoot<User> {

  @NotBlank
  @Size(max = 50)
  @Column(unique = true)
  private String email;

  @NotBlank
  @Size(max = 120)
  @Column(nullable = false)
  private String password;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
          name = "user_roles",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "role_id")
  )
  private Set<Role> roles;

  private String name;
  private String lastName;
  private String imageUrl;
  private Double salary;

  @Size(max = 20)
  private String phone;

  @Min(18)
  @Max(100)
  private Integer age;

  @Size(max = 500)
  @Column(length = 500)
  private String bio;

  @Enumerated(EnumType.STRING)
  private UserStatus status = UserStatus.OFFLINE;

  @Column(name = "last_activity")
  private LocalDateTime lastActivity;

  @ManyToMany
  @JoinTable(
          name = "member_projects",
          joinColumns = @JoinColumn(name = "user_id"),
          inverseJoinColumns = @JoinColumn(name = "project_id")
  )
  private Set<Project> memberInProjects;

  protected User(){
      super();
      this.roles = new HashSet<>();
      this.memberInProjects = new HashSet<>();
  }

  public User(String email, String password) {
      this.email = email;
      this.password = password;
      this.roles = new HashSet<>();
      this.memberInProjects = new HashSet<>();
  }

  public User(String email, String password, String name, String lastName, List<Role> roles) {
    this(email, password);
    this.name = name;
    this.lastName = lastName;
    addRoles(roles);
  }

  public User updateUserDetails(UpdateUserCommand updateUserCommand) {
      this.name = updateUserCommand.name();
      this.lastName = updateUserCommand.lastName();
      this.imageUrl = updateUserCommand.imageUrl();
      this.salary = updateUserCommand.salary();
      this.phone = updateUserCommand.phone();
      this.age = updateUserCommand.age();
      this.bio = updateUserCommand.bio();
      return this;
  }

  public void assignToProject(Project project) {
    this.memberInProjects.add(project);
  }

  public void removeFromProject(Long projectId) {
    this.memberInProjects.removeIf(project -> project.getId().equals(projectId));
  }

  public void addRoles(List<Role> roles) {
    var validatedRoleSet = Role.validateRoleSet(roles);
    this.roles.addAll(validatedRoleSet);
  }

    public void setOnline() {
        this.status = UserStatus.ONLINE;
        this.lastActivity = LocalDateTime.now();
    }

    public String getLastActivityFormatted() {
        if (lastActivity == null) return "Never";
        Duration duration = Duration.between(lastActivity, LocalDateTime.now());
        long minutes = duration.toMinutes();

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + " minutes ago";
        if (minutes < 1440) return (minutes / 60) + " hours ago";
        return (minutes / 1440) + " days ago";
    }
}
