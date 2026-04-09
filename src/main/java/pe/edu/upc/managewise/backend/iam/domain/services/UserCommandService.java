package pe.edu.upc.managewise.backend.iam.domain.services;

import org.apache.commons.lang3.tuple.ImmutablePair;
import pe.edu.upc.managewise.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.managewise.backend.iam.domain.model.commands.*;

import java.util.Optional;

public interface UserCommandService {

    Optional<ImmutablePair<User, String>> handle(SignInCommand signInCommand);

    Optional<User> handle(SignUpCommand signUpCommand);

    Optional<User> handle(UpdateUserCommand updateUserCommand , Long userId);

    void handle(DeleteUserCommand deleteUserCommand);

    Optional<User> handle(LeaveProjectCommand leaveProjectCommand);
}
