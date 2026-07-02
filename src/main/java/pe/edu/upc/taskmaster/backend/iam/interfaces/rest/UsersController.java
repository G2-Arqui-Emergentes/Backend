package pe.edu.upc.taskmaster.backend.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.DeleteUserCommand;
import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.taskmaster.backend.iam.domain.model.queries.GetAllUsersQuery;
import pe.edu.upc.taskmaster.backend.iam.domain.model.queries.GetUserByEmailQuery;
import pe.edu.upc.taskmaster.backend.iam.domain.model.queries.GetUserByIdQuery;
import pe.edu.upc.taskmaster.backend.iam.domain.services.UserCommandService;
import pe.edu.upc.taskmaster.backend.iam.domain.services.UserQueryService;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.ChangePasswordResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.UpdateUserResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.ChangePasswordCommandFromResourceAssembler;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.UpdateUserCommandFromResourceAssembler;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.List;

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

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Retrieves a list of all registered users. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    })
    public ResponseEntity<List<UserResource>> getAllUsers() {
        var getAllUsersQuery = new GetAllUsersQuery();
        var users = userQueryService.handle(getAllUsersQuery);
        var userResources = users.stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(userResources);
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves a specific user by their unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
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

    @PutMapping
    @Operation(
            summary = "Update current user",
            description = "Updates the profile information of the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<UserResource> updateUser(@RequestBody UpdateUserResource updateUserResource){
        Long userId = getUserIdFromContext();
        UpdateUserCommand updateUserCommand = UpdateUserCommandFromResourceAssembler.toCommandFromResource(updateUserResource);

        var userOptional = userCommandService.handle(updateUserCommand,userId);

        if (userOptional.isPresent()) {
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
            return ResponseEntity.ok(userResource);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{userId}")
    @Operation(
            summary = "Delete user",
            description = "Deletes a user by their ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(userId);
        userCommandService.handle(deleteUserCommand);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(
            summary = "Get user by email",
            description = "Retrieves a user by their email address"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> getUserByEmail(@PathVariable String email) {
        GetUserByEmailQuery getUserByEmailQuery = new GetUserByEmailQuery(email);
        var userOptional = userQueryService.handle(getUserByEmailQuery);
        if (userOptional.isPresent()) {
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
            return ResponseEntity.ok(userResource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password",
            description = "Changes the password of the authenticated user. Requires current password verification."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input - Current password incorrect or new password invalid"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordResource changePasswordResource) {
        Long userId = getUserIdFromContext();
        var changePasswordCommand = ChangePasswordCommandFromResourceAssembler
                .toCommandFromResource(userId, changePasswordResource);

        userCommandService.handle(changePasswordCommand);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get current user profile",
            description = "Retrieves the profile of the currently authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> getCurrentUser() {
        Long userId = getUserIdFromContext();
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    @PutMapping("/{userId}")
    @Operation(
            summary = "Update user by ID",
            description = "Updates a user's profile by ID. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResource> updateUserById(
            @PathVariable Long userId,
            @RequestBody UpdateUserResource updateUserResource) {
        UpdateUserCommand updateUserCommand = UpdateUserCommandFromResourceAssembler
                .toCommandFromResource(updateUserResource);

        var userOptional = userCommandService.handle(updateUserCommand, userId);

        if (userOptional.isPresent()) {
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
            return ResponseEntity.ok(userResource);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
