package pe.edu.upc.managewise.backend.iam.domain.services;

import pe.edu.upc.managewise.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUsersByProjectIdQuery;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
  List<User> handle(GetAllUsersQuery getAllUsersQuery);

  Optional<User> handle(GetUserByIdQuery getUserByIdQuery);

  Optional<User> handle(GetUserByEmailQuery getUserByEmailQuery);

  List<User> handle(GetUsersByProjectIdQuery getUsersByProjectIdQuery);
}
