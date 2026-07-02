package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.taskmaster.backend.iam.domain.model.entities.Role;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.UserResource;

public class UserResourceFromEntityAssembler {
  public static UserResource toResourceFromEntity(User user) {
      return new UserResource(
            user.getId(),
            user.getEmail(),
            user.getRoles().stream().map(Role::getName).toList(),
            user.getName(),
            user.getLastName(),
            user.getImageUrl(),
            user.getSalary(),
            user.getPhone(),
            user.getAge(),
            user.getBio(),
            user.getStatus(),
            user.getLastActivityFormatted(),
            user.getMemberInProjects().stream().map(project -> project.getId()).toList()
    );
  }
}
