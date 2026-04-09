package pe.edu.upc.managewise.backend.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.managewise.backend.iam.domain.model.commands.DeleteUserCommand;
import pe.edu.upc.managewise.backend.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.managewise.backend.iam.domain.model.queries.GetUsersByProjectIdQuery;
import pe.edu.upc.managewise.backend.iam.domain.services.UserCommandService;
import pe.edu.upc.managewise.backend.iam.domain.services.UserQueryService;
import pe.edu.upc.managewise.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.UpdateUserResource;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.transform.UpdateUserCommandFromResourceAssembler;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.List;

/**
 * This class is a REST controller that exposes the users resource.
 * It includes the following operations:
 * - GET /api/v1/users: returns all the users
 * - GET /api/v1/users/{userId}: returns the user with the given id
 **/
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User Management Endpoints")
public class UsersController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    private Long getUserIdFromContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new RuntimeException("Invalid principal type");
    }

    /**
    * This method returns all the users.
    *
    * @return a list of user resources.
    * @see UserResource
    */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users.")
    @ApiResponses(value = {
            @ApiResponse (responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse (responseCode = "404", description = "No users found")
    })
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(userResources);
    }

    /**
    * This method returns the user with the given id.
    *
    * @param userId the user id.
    * @return the user resource with the given id
    * @throws RuntimeException if the user is not found
    * @see UserResource
    */
    @GetMapping("/{userId}")
    @Operation(summary = "Get a user by ID", description = "Retrieves a user by its ID.")
    @ApiResponses(value = {
            @ApiResponse (responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse (responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    /**
     * This method updates a user.
     *
     * @param updateUserResource the user resource to be updated.
     * @return the updated user resource.
     * @throws RuntimeException if the user is not found.
     * @see UserResource
     * @see UpdateUserResource
     */
    @PutMapping
    @Operation(summary = "Update a user", description = "Update a user by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse (responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserResource> updateUser(@RequestBody UpdateUserResource updateUserResource){
        Long userId = getUserIdFromContext();
        UpdateUserCommand updateUserCommand = UpdateUserCommandFromResourceAssembler.toCommandFromResource(updateUserResource);

        var userOptional = userCommandService.handle(updateUserCommand,userId);

        if (userOptional.isPresent()) {
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
            return ResponseEntity.ok(userResource); // 200 OK
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * This method deletes a user.
     *
     * @param userId the user id.
     * @return a response entity with no content.
     * @throws RuntimeException if the user is not found.
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user", description = "Deletes a user by its ID.")
    @ApiResponses(value = {
            @ApiResponse (responseCode = "204", description = "User deleted successfully"),
            @ApiResponse (responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> deleteUser(@PathVariable Long userId) {
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(userId);
        userCommandService.handle(deleteUserCommand);
        return ResponseEntity.noContent().build();
    }

    /**
     * This method returns the user with the given email.
     *
     * @param email the user email.
     * @return the user resource with the given email
     * @throws RuntimeException if the user is not found
     * @see UserResource
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "Get a user by email", description = "Retrieves a user by email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> getUserByEmail(@PathVariable String email) {

        GetUserByEmailQuery getUserByEmailQuery = new GetUserByEmailQuery(email);
        var userOptional =userQueryService.handle(getUserByEmailQuery);
        if (userOptional.isPresent()) {
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
            return ResponseEntity.ok(userResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

//    @GetMapping("/project/{projectId}")
//    @Operation(summary = "Get users by project ID")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
//            @ApiResponse(responseCode = "404", description = "No users found for this project")
//    })
//    public ResponseEntity<List<UserResource>> getUsersByProjectId(@PathVariable Long projectId) {
//        // Create the query to get users by group ID
//        GetUsersByProjectIdQuery getUsersByProjectIdQuery = new GetUsersByProjectIdQuery(projectId);
//
//        // Execute the query
//        var users = userQueryService.handle(getUsersByProjectIdQuery);
//
//        // Check if users were found
//        if (users.isEmpty()) return ResponseEntity.notFound().build();
//
//        // Convert the list of users to resources
//        var userResources = users.stream()
//                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
//                .toList();
//
//        return ResponseEntity.ok(userResources);
//    }

}
