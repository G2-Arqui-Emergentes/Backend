package pe.edu.upc.managewise.backend.iam.application.internal.commandservices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.iam.application.internal.outboundservices.hashing.HashingService;
import pe.edu.upc.managewise.backend.iam.application.internal.outboundservices.tokens.TokenService;
import pe.edu.upc.managewise.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.managewise.backend.iam.domain.model.commands.*;
import pe.edu.upc.managewise.backend.iam.domain.services.UserCommandService;
import pe.edu.upc.managewise.backend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import pe.edu.upc.managewise.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;

/**
* User command service implementation
* <p>
*     This class implements the {@link UserCommandService} interface and provides the implementation for the
*     {@link SignInCommand} and {@link SignUpCommand} commands.
* </p>
*/
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository) {
    this.userRepository = userRepository;
    this.hashingService = hashingService;
    this.tokenService = tokenService;
    this.roleRepository = roleRepository;
    }

    /**
    * Handle the sign-in command
    * <p>
    *     This method handles the {@link SignInCommand} command and returns the user and the token.
    * </p>
    * @param signInCommand the sign-in command containing the username and password
    * @return and optional containing the user matching the username and the generated token
    * @throws RuntimeException if the user is not found or the password is invalid
    */
    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand signInCommand) {
    var user = userRepository.findByEmail(signInCommand.email());
    if (user.isEmpty())
      throw new RuntimeException("User not found");
    if (!hashingService.matches(signInCommand.password(), user.get().getPassword()))
      throw new RuntimeException("Invalid password");

    var token = tokenService.generateToken(user.get().getEmail());
    return Optional.of(ImmutablePair.of(user.get(), token));
    }

    /**
    * Handle the sign-up command
    * <p>
    *     This method handles the {@link SignUpCommand} command and returns the user.
    * </p>
    * @param signUpCommand the sign-up command containing the username and password
    * @return the created user
    */
    @Override
    public Optional<User> handle(SignUpCommand signUpCommand) {
    if (userRepository.existsByEmail(signUpCommand.email()))
        throw new RuntimeException("User with this email already exists");
    var roles = signUpCommand.roles().stream()
        .map(role ->
            roleRepository.findByName(role)
                .orElseThrow(() -> new RuntimeException("Role name not found")))
        .toList();
    var user = new User(signUpCommand.email(), hashingService.encode(signUpCommand.password()), signUpCommand.name(), signUpCommand.lastName(), roles);
    userRepository.save(user);
    return userRepository.findByEmail(signUpCommand.email());
    }

    @Override
    public Optional<User> handle(UpdateUserCommand updateUserCommand, Long userId) {
        var userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + userId + " not found");
        }

        var userToUpdate = userOptional.get();

        try {
            var updatedUser = userToUpdate.updateUserDetails(updateUserCommand);
            var savedUser = userRepository.save(updatedUser);
            return Optional.of(savedUser);
        } catch (Exception e) {
            throw new RuntimeException("Error updating user: " + e.getMessage(), e);
        }
    }

    @Override
    public void handle(DeleteUserCommand deleteUserCommand) {
        if (!userRepository.existsById(deleteUserCommand.userId())) {
            throw new IllegalArgumentException("User with ID " + deleteUserCommand.userId() + " not found");
        }

        try{
            userRepository.deleteById(deleteUserCommand.userId());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> handle(LeaveProjectCommand leaveProjectCommand) {
        var userOptional = userRepository.findById(leaveProjectCommand.userId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + leaveProjectCommand.userId() + " not found");
        }

        var user = userOptional.get();

        boolean belongsToCourse = user.getMemberInProjects().stream()
                .anyMatch(course -> course.getId().equals(leaveProjectCommand.projectId()));

        if (!belongsToCourse) {
            throw new IllegalArgumentException("User does not belong to the project with ID " + leaveProjectCommand.projectId());
        }
        user.removeFromProject(leaveProjectCommand.projectId());

        try {
            userRepository.save(user);
            return Optional.of(user);
        } catch (Exception e) {
            throw new RuntimeException("Error while removing user from project", e);
        }
    }
}
