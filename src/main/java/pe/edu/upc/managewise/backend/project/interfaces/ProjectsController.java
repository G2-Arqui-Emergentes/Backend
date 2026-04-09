package pe.edu.upc.managewise.backend.project.interfaces;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.managewise.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.managewise.backend.project.domain.model.aggregates.Project;
import pe.edu.upc.managewise.backend.project.domain.model.commands.AddUserToProjectCommand;
import pe.edu.upc.managewise.backend.project.domain.model.commands.DeleteProjectCommand;
import pe.edu.upc.managewise.backend.project.domain.model.commands.RemoveUserFromProjectCommand;
import pe.edu.upc.managewise.backend.project.domain.model.queries.*;
import pe.edu.upc.managewise.backend.project.domain.services.ProjectCommandService;
import pe.edu.upc.managewise.backend.project.domain.services.ProjectQueryService;
import pe.edu.upc.managewise.backend.project.interfaces.rest.resources.*;
import pe.edu.upc.managewise.backend.project.interfaces.rest.transform.*;

import java.util.List;

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
            System.out.println("Authenticated User ID: " + userDetails.getId());
            return userDetails.getId();
        }
        throw new RuntimeException("Invalid principal type");
    }

    @PostMapping
    @Operation(summary = "Create a Project", description = "Creates a new project with the provided details.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Project Created Successfully"),
                    @ApiResponse(responseCode = "404", description = "Invalid input data")
            }
    )
    public ResponseEntity<ProjectResource> createProject(@RequestBody CreateProjectResource resource) {
        Long userId = getAuthenticatedUserId();

        var createCommand = CreateProjectCommandFromResourceAssembler.toCommandFromResource(resource,userId);

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
        return ResponseEntity.ok(projectResponse);
    }

    @PutMapping(value = "/{id}")
    @Operation(summary = "Update a project", description = "Updates an existing project by its id.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Project updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Project with specified id does not exist")
            }
    )
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

    @DeleteMapping(value = "/{id}")
    @Operation(summary = "Delete project", description = "Delete the project with the specified id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Project with the specified id does not exist")
            }
    )
    public ResponseEntity<Void> deleteProject(@PathVariable("id") Long id) {
        var deleteProjectCommand = new DeleteProjectCommand(id);
        projectCommandService.handle(deleteProjectCommand);

        return ResponseEntity.noContent().build();
    }

    @GetMapping(value ="/join/{key}")
    @Operation(summary = "Join a project by code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully joined the project"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<ProjectResource> joinCourse(
            @PathVariable String key
    ) {

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
    @Operation(summary = "Remove user from project", description = "Removes a user from the specified project.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "204", description = "User removed from project successfully"),
                    @ApiResponse(responseCode = "404", description = "Project with the specified id does not exist")
            }
    )
    public ResponseEntity<?> removeUserFromProject(
            @PathVariable Long projectId,
            @PathVariable Long memberId) {

        Long leaderId = getAuthenticatedUserId();

        RemoveUserFromProjectCommand command = new RemoveUserFromProjectCommand(memberId, projectId);

        projectCommandService.handle(command, leaderId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("{projectId}/code")
    @Operation(summary = "Set project code", description = "Sets a unique code for the specified project.")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Code successfully set"),
                    @ApiResponse(responseCode = "404", description = "Project not found or project already has a code")
            }
    )
    public ResponseEntity<ProjectCodeResource> setProjectCodeByProjectId(@PathVariable Long projectId, @RequestBody SetCodeResource resource) {

        var setProjectCodeCommand =
                SetCodeCommandFromResourceAssembler.toCommandFromResource(projectId, resource);

        var code = this.projectCommandService.handle(setProjectCodeCommand);

        if (code.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var codeResponse =
                ProjectCodeResourceFromEntityAssembler.toResourceFromEntity(code.get());

        return ResponseEntity.ok(codeResponse);
    }

    @GetMapping
    @Operation(summary = "Get all projects", description = "Gets all projects")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Projects retrieved Successfully"),
                    @ApiResponse(responseCode = "404", description = "Could not retrieve projects")
            }
    )
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
    @Operation(summary = "Get project by id", description = "Retrieves a project with the specified id")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Project retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Project with specified id does not exist")
            }
    )
    public ResponseEntity<ProjectResource> getProjectById(@PathVariable("projectId") Long projectId) {
        var getProjectByIdQuery = new GetProjectByIdQuery(projectId);
        var optionalProject = this.projectQueryService.handle(getProjectByIdQuery);
        if (optionalProject.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var projectResource = ProjectResourceFromEntityAssembler.toResourceFromEntity(optionalProject.get());
        return ResponseEntity.ok(projectResource);
    }

    @GetMapping("/member")
    @Operation(summary = "Get projects by member ID", description = "Retrieves all projects that a member belongs to")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found or no projects found for member")
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
            summary = "Get projects by leader ID",
            description = "Retrieves all projects that a leader owns. Uses the authenticated leader's ID from JWT token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projects retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Leader not found or no projects found for leader"
            )
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
}
