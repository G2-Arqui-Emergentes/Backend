package pe.edu.upc.taskmaster.backend.task.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.taskmaster.backend.task.domain.model.commands.DeleteTaskCommand;
import pe.edu.upc.taskmaster.backend.task.domain.model.queries.*;
import pe.edu.upc.taskmaster.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.taskmaster.backend.task.domain.model.valueobjects.Status;
import pe.edu.upc.taskmaster.backend.task.domain.services.TaskCommandService;
import pe.edu.upc.taskmaster.backend.task.domain.services.TaskQueryService;
import pe.edu.upc.taskmaster.backend.task.interfaces.rest.resources.*;
import pe.edu.upc.taskmaster.backend.task.interfaces.rest.transform.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tasks", description = "Task Management Endpoints")
public class TasksController {

    private final TaskCommandService taskCommandService;
    private final TaskQueryService taskQueryService;

    public TasksController(TaskCommandService taskCommandService, TaskQueryService taskQueryService) {
        this.taskCommandService = taskCommandService;
        this.taskQueryService = taskQueryService;
    }

    @GetMapping
    @Operation(
            summary = "Get all tasks",
            description = "Retrieves a list of all tasks in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<TaskResource>> getAllTasks() {
        var getAllTasksQuery = new GetAllTasksQuery();
        var tasks = taskQueryService.handle(getAllTasksQuery);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/{taskId}")
    @Operation(
            summary = "Get task by ID",
            description = "Retrieves a specific task by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResource> getTaskById(@PathVariable Long taskId) {
        var getTaskByIdQuery = new GetTaskByIdQuery(taskId);
        var task = taskQueryService.handle(getTaskByIdQuery);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @GetMapping("/project/{projectId}")
    @Operation(
            summary = "Get tasks by project",
            description = "Retrieves all tasks belonging to a specific project"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    public ResponseEntity<List<TaskResource>> getTasksByProjectId(@PathVariable Long projectId) {
        var getTasksByProjectIdQuery = new GetTasksByProjectIdQuery(projectId);
        var tasks = taskQueryService.handle(getTasksByProjectIdQuery);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/user/{userId}")
    @Operation(
            summary = "Get tasks by assigned user",
            description = "Retrieves all tasks assigned to a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<List<TaskResource>> getTasksByAssignedUserId(@PathVariable Long userId) {
        var getTasksByAssignedUserIdQuery = new GetTasksByAssignedUserIdQuery(userId);
        var tasks = taskQueryService.handle(getTasksByAssignedUserIdQuery);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/project/{projectId}/user/{userId}")
    @Operation(
            summary = "Get tasks by project and assigned user",
            description = "Retrieves tasks for a specific project assigned to a specific user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<TaskResource>> getTasksByProjectIdAndAssignedUserId(
            @PathVariable Long projectId, @PathVariable Long userId) {
        var query = new GetTasksByProjectIdAndAssignedUserIdQuery(projectId, userId);
        var tasks = taskQueryService.handle(query);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/project/{projectId}/status/{status}")
    @Operation(
            summary = "Get tasks by status",
            description = "Retrieves tasks in a specific project filtered by status"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<TaskResource>> getTasksByStatus(
            @PathVariable Long projectId, @PathVariable String status) {
        try {
            var statusEnum = Status.valueOf(status.toUpperCase());
            var query = new GetTasksByStatusQuery(projectId, statusEnum);
            var tasks = taskQueryService.handle(query);
            var taskResources = tasks.stream()
                    .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(taskResources);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/project/{projectId}/priority/{priority}")
    @Operation(
            summary = "Get tasks by priority",
            description = "Retrieves tasks in a specific project filtered by priority"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Tasks retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid priority value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<List<TaskResource>> getTasksByPriority(
            @PathVariable Long projectId, @PathVariable String priority) {
        try {
            var priorityEnum = Priority.valueOf(priority.toUpperCase());
            var query = new GetTasksByPriorityQuery(projectId, priorityEnum);
            var tasks = taskQueryService.handle(query);
            var taskResources = tasks.stream()
                    .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(taskResources);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(
            summary = "Create a task",
            description = "Creates a new task in a project"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
    public ResponseEntity<TaskResource> createTask(@RequestBody CreateTaskResource resource) {
        var createTaskCommand = CreateTaskCommandFromResourceAssembler.toCommandFromResource(resource);
        var task = taskCommandService.handle(createTaskCommand);
        if (task.isEmpty()) return ResponseEntity.badRequest().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return new ResponseEntity<>(taskResource, HttpStatus.CREATED);
    }

    @PutMapping("/{taskId}")
    @Operation(
            summary = "Update a task",
            description = "Updates an existing task's details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResource> updateTask(
            @PathVariable Long taskId, @RequestBody UpdateTaskResource resource) {
        var updateTaskCommand = UpdateTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        var task = taskCommandService.handle(updateTaskCommand);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @PutMapping("/{taskId}/status")
    @Operation(
            summary = "Update task status",
            description = "Updates the status of a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Task status updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskResource> updateTaskStatus(
            @PathVariable Long taskId, @RequestBody UpdateTaskStatusResource resource) {
        var updateStatusCommand = UpdateTaskStatusCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        var task = taskCommandService.handle(updateStatusCommand);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @PutMapping("/{taskId}/assign")
    @Operation(
            summary = "Assign user to task",
            description = "Assigns a user to a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User assigned to task successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task or user not found")
    })
    public ResponseEntity<TaskResource> assignUserToTask(
            @PathVariable Long taskId, @RequestBody AssignUserToTaskResource resource) {
        var assignCommand = AssignUserToTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        var task = taskCommandService.handle(assignCommand);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @PutMapping("/{taskId}/unassign")
    @Operation(
            summary = "Remove user from task",
            description = "Removes a user's assignment from a specific task"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User removed from task successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskResource.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task or user not found")
    })
    public ResponseEntity<TaskResource> removeUserFromTask(
            @PathVariable Long taskId, @RequestBody RemoveUserFromTaskResource resource) {
        var removeCommand = RemoveUserFromTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        taskCommandService.handle(removeCommand);

        var getTaskQuery = new GetTaskByIdQuery(taskId);
        var updatedTask = taskQueryService.handle(getTaskQuery);
        if (updatedTask.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(updatedTask.get());
        return ResponseEntity.ok(taskResource);
    }

    @DeleteMapping("/{taskId}")
    @Operation(
            summary = "Delete a task",
            description = "Deletes a specific task by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<String> deleteTask(@PathVariable Long taskId) {
        var deleteCommand = new DeleteTaskCommand(taskId);
        try {
            taskCommandService.handle(deleteCommand);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
