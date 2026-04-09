package pe.edu.upc.managewise.backend.iam.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUsersByProjectIdQuery;
import pe.edu.upc.managewise.backend.iam.domain.services.UserQueryService;
import pe.edu.upc.managewise.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link UserQueryService} interface.
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {
  private final UserRepository userRepository;

    /**
    * Constructor.
    *
    * @param userRepository {@link UserRepository} instance.
    */
    public UserQueryServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
    }

    /**
    * This method is used to handle {@link GetAllUsersQuery} query.
    * @param getAllUsersQuery {@link GetAllUsersQuery} instance.
    * @return {@link List} of {@link User} instances.
    * @see GetAllUsersQuery
    */
    @Override
    public List<User> handle(GetAllUsersQuery getAllUsersQuery) {
    return userRepository.findAll();
    }

    /**
    * This method is used to handle {@link GetUserByIdQuery} query.
    * @param getUserByIdQuery {@link GetUserByIdQuery} instance.
    * @return {@link Optional} of {@link User} instance.
    * @see GetUserByIdQuery
    */
    @Override
    public Optional<User> handle(GetUserByIdQuery getUserByIdQuery) {
    return userRepository.findById(getUserByIdQuery.userId());
    }

    /**
    * This method is used to handle {@link GetUserByEmailQuery} query.
    * @param getUserByEmailQuery {@link GetUserByEmailQuery} instance.
    * @return {@link Optional} of {@link User} instance.
    * @see GetUserByEmailQuery
    */
    @Override
    public Optional<User> handle(GetUserByEmailQuery getUserByEmailQuery) {
    return userRepository.findByEmail(getUserByEmailQuery.email());
    }

    @Override
    public List<User> handle(GetUsersByProjectIdQuery query) {
        return userRepository.findAll().stream()
                .filter(user -> user.getMemberInProjects().stream()
                        .anyMatch(project -> project.getId().equals(query.projectId())))
                .toList();
    }
}
