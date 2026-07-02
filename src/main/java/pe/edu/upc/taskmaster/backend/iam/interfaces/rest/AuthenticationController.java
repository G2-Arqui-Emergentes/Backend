package pe.edu.upc.taskmaster.backend.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.taskmaster.backend.iam.domain.services.UserCommandService;
import pe.edu.upc.taskmaster.backend.iam.domain.services.UserQueryService;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.SignInResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.SignUpResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Authentication Endpoints")
public class AuthenticationController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public AuthenticationController(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Sign in user with email and password")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "User signed in successfully"),
                    @ApiResponse(responseCode = "400", description = "Bad Request - Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Not Found - User does not exist"),
            }
    )
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        var authenticatedUser = userCommandService.handle(signInCommand);

        if (authenticatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var user = authenticatedUser.get().getLeft();
        user.setOnline();
        userQueryService.updateUserStatus(user);

        var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(authenticatedUser.get().getLeft(), authenticatedUser.get().getRight());
        return ResponseEntity.ok(authenticatedUserResource);
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Sign-up with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }
}
