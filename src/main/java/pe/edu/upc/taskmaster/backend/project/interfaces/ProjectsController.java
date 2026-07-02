package pe.edu.upc.taskmaster.backend.project.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.UserResource;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import pe.edu.upc.taskmaster.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.taskmaster.backend.project.domain.model.commands.AddUserToProjectCommand;
import pe.edu.upc.taskmaster.backend.project.domain.model.commands.DeleteProjectCommand;
import pe.edu.upc.taskmaster.backend.project.domain.model.commands.RemoveUserFromProjectCommand;
import pe.edu.upc.taskmaster.backend.project.domain.model.queries.GetAllProjectsQuery;
import pe.edu.upc.taskmaster.backend.project.domain.model.queries.GetProjectByIdQuery;
import pe.edu.upc.taskmaster.backend.project.domain.model.queries.GetProjectsByLeaderIdQuery;
import pe.edu.upc.taskmaster.backend.project.domain.model.queries.GetProjectsByMemberIdQuery;
import pe.edu.upc.taskmaster.backend.project.domain.services.ProjectCommandService;
import pe.edu.upc.taskmaster.backend.project.domain.services.ProjectQueryService;
import pe.edu.upc.taskmaster.backend.project.interfaces.rest.resources.*;
import pe.edu.upc.taskmaster.backend.project.interfaces.rest.transform.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/api/v1/projects", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Projects", description = "Project Management Endpoints")
public class ProjectsController {

    private final ProjectQueryService projectQueryService;
    private final ProjectCommandService projectCommandService;

    public ProjectsController(ProjectQueryService projectQueryService, ProjectCommandService projectCommandService) {
        this.projectQueryService = projectQueryService;
        this.projectCommandService = projectCommandService;
    }

    private Long getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new RuntimeException("Invalid principal type");
    }

    @PostMapping
    @Operation(
            summary = "Create a project",
            description = "Creates a new project with the authenticated user as the project leader"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Project created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<ProjectResource> createProject(@RequestBody CreateProjectResource resource) {
        Long userId = getAuthenticatedUserId();

        var createCommand = CreateProjectCommandFromResourceAssembler.toCommandFromResource(resource, userId);

        var createdId = projectCommandService.handle(createCommand);

        if (createdId == null || createdId <= 0L) {
            return ResponseEntity.badRequest().build();
        }

        var project = projectQueryService.handle(new GetProjectByIdQuery(createdId));

        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var projectEntity = project.get();
        var projectResponse = ProjectResourceFromEntityAssembler.toResourceFromEntity(projectEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectResponse);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a project",
            description = "Updates an existing project's details. Only the project leader can update."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only project leader can update"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResource> updateProject(@RequestBody UpdateProjectResource resource, @PathVariable("id") Long id) {
        Long userId = getAuthenticatedUserId();

        var updateCommand = UpdateProjectCommandFromResourceAssembler.toCommandFromResource(resource, id);

        var updatedProject = projectCommandService.handle(updateCommand);

        if (updatedProject.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var getProjectByIdQuery = new GetProjectByIdQuery(id);
        var project = projectQueryService.handle(getProjectByIdQuery);
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var projectEntity = project.get();
        var projectResponse = ProjectResourceFromEntityAssembler.toResourceFromEntity(projectEntity);
        return ResponseEntity.ok(projectResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a project",
            description = "Deletes a project by ID. Only the project leader can delete."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only project leader can delete"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<Void> deleteProject(@PathVariable("id") Long id) {
        var deleteProjectCommand = new DeleteProjectCommand(id);
        projectCommandService.handle(deleteProjectCommand);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/join/{key}")
    @Operation(
            summary = "Join a project",
            description = "Allows a user to join a project using the project's unique join key"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully joined the project",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid join key or user already in project"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResource> joinProject(@PathVariable String key) {
        Long userId = getAuthenticatedUserId();
        var addUserToProjectCommand = new AddUserToProjectCommand(userId, key);
        var projectOptional = projectCommandService.handle(addUserToProjectCommand);

        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            ProjectResource resource = ProjectResourceFromEntityAssembler.toResourceFromEntity(project);
            return ResponseEntity.ok(resource);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @Operation(
            summary = "Remove user from project",
            description = "Removes a user from a project. Only the project leader can remove members."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User removed from project successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Only project leader can remove members"),
            @ApiResponse(responseCode = "404", description = "Project or user not found")
    })
    public ResponseEntity<Void> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {
        Long leaderId = getAuthenticatedUserId();
        RemoveUserFromProjectCommand command = new RemoveUserFromProjectCommand(memberId, projectId);
        projectCommandService.handle(command, leaderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Get all projects",
            description = "Retrieves a list of all projects in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<ProjectResource>> getAllProjects() {
        var getAllProjectsQuery = new GetAllProjectsQuery();
        var projects = projectQueryService.handle(getAllProjectsQuery);

        if (projects == null || projects.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        var projectResponse = projects.stream().map(ProjectResourceFromEntityAssembler::toResourceFromEntity).toList();
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/{projectId}")
    @Operation(
            summary = "Get project by ID",
            description = "Retrieves a specific project by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Project retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResource> getProjectById(@PathVariable("projectId") Long projectId) {
        var getProjectByIdQuery = new GetProjectByIdQuery(projectId);
        var optionalProject = this.projectQueryService.handle(getProjectByIdQuery);
        if (optionalProject.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var projectResource = ProjectResourceFromEntityAssembler.toResourceFromEntity(optionalProject.get());
        return ResponseEntity.ok(projectResource);
    }

    @GetMapping("/member")
    @Operation(
            summary = "Get projects by member",
            description = "Retrieves all projects that the authenticated user is a member of"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<ProjectResource>> getProjectsByMemberId() {
        Long userId = getAuthenticatedUserId();
        var getProjectsByMemberIdQuery = new GetProjectsByMemberIdQuery(userId);

        var projects = projectQueryService.handle(getProjectsByMemberIdQuery);

        if (projects.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        var projectResponse = projects.stream()
                .map(ProjectResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/leader")
    @Operation(
            summary = "Get projects by leader",
            description = "Retrieves all projects where the authenticated user is the project leader"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<ProjectResource>> getProjectsByLeaderId() {
        Long leaderId = getAuthenticatedUserId();
        var getProjectsByLeaderIdQuery = new GetProjectsByLeaderIdQuery(leaderId);

        var projects = projectQueryService.handle(getProjectsByLeaderIdQuery);
        if (projects.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        var projectsResponse = projects.stream()
                .map(ProjectResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(projectsResponse);
    }

    @GetMapping("/{projectId}/members")
    @Operation(
            summary = "Get project members",
            description = "Retrieves all members of a specific project"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Members retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<List<UserResource>> getProjectMembers(@PathVariable Long projectId) {
        var getProjectByIdQuery = new GetProjectByIdQuery(projectId);
        var project = projectQueryService.handle(getProjectByIdQuery);
        if (project.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var members = project.get().getMembers().stream()
                .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(members);
    }
}
