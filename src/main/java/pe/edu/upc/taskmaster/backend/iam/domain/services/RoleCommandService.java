package pe.edu.upc.taskmaster.backend.iam.domain.services;

import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
  void handle(SeedRolesCommand command);
}
