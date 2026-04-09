package pe.edu.upc.managewise.backend.iam.domain.model.aggregates;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.managewise.backend.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.managewise.backend.iam.domain.model.entities.Role;
import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

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
  private String password;

  @ManyToMany(fetch = FetchType.EAGER,cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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
}
