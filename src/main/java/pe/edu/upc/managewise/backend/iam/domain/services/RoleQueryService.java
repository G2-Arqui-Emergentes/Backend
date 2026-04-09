package pe.edu.upc.managewise.backend.iam.domain.services;

import pe.edu.upc.managewise.backend.iam.domain.model.entities.Role;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetAllRolesQuery;

import java.util.List;

public interface RoleQueryService {
  List<Role> handle(GetAllRolesQuery query);
}
