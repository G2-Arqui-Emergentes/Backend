package pe.edu.upc.managewise.backend.task.interfaces.rest;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.managewise.backend.task.domain.model.commands.DeleteTaskCommand;
import pe.edu.upc.managewise.backend.task.domain.model.queries.*;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;
import pe.edu.upc.managewise.backend.task.domain.services.TaskCommandService;
import pe.edu.upc.managewise.backend.task.domain.services.TaskQueryService;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.*;
import pe.edu.upc.managewise.backend.task.interfaces.rest.transform.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task Management Endpoints")
public class TasksController {

    private final TaskCommandService taskCommandService;
    private final TaskQueryService taskQueryService;

    public TasksController(TaskCommandService taskCommandService, TaskQueryService taskQueryService) {
        this.taskCommandService = taskCommandService;
        this.taskQueryService = taskQueryService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResource>> getAllTasks() {
        var getAllTasksQuery = new GetAllTasksQuery();
        var tasks = taskQueryService.handle(getAllTasksQuery);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskResource> getTaskById(@PathVariable Long taskId) {
        var getTaskByIdQuery = new GetTaskByIdQuery(taskId);
        var task = taskQueryService.handle(getTaskByIdQuery);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResource>> getTasksByProjectId(@PathVariable Long projectId) {
        var getTasksByProjectIdQuery = new GetTasksByProjectIdQuery(projectId);
        var tasks = taskQueryService.handle(getTasksByProjectIdQuery);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResource>> getTasksByAssignedUserId(@PathVariable Long userId) {
        var getTasksByAssignedUserIdQuery = new GetTasksByAssignedUserIdQuery(userId);
        var tasks = taskQueryService.handle(getTasksByAssignedUserIdQuery);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/project/{projectId}/user/{userId}")
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
    public ResponseEntity<List<TaskResource>> getTasksByStatus(
            @PathVariable Long projectId, @PathVariable String status) {
        var statusEnum = Status.valueOf(status.toUpperCase());
        var query = new GetTasksByStatusQuery(projectId, statusEnum);
        var tasks = taskQueryService.handle(query);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @GetMapping("/project/{projectId}/priority/{priority}")
    public ResponseEntity<List<TaskResource>> getTasksByPriority(
            @PathVariable Long projectId, @PathVariable String priority) {
        var priorityEnum = Priority.valueOf(priority.toUpperCase());
        var query = new GetTasksByPriorityQuery(projectId, priorityEnum);
        var tasks = taskQueryService.handle(query);
        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskResources);
    }

    @PostMapping
    public ResponseEntity<TaskResource> createTask(@RequestBody CreateTaskResource resource) {
        var createTaskCommand = CreateTaskCommandFromResourceAssembler.toCommandFromResource(resource);
        var task = taskCommandService.handle(createTaskCommand);
        if (task.isEmpty()) return ResponseEntity.badRequest().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return new ResponseEntity<>(taskResource, HttpStatus.CREATED);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResource> updateTask(
            @PathVariable Long taskId, @RequestBody UpdateTaskResource resource) {
        var updateTaskCommand = UpdateTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        var task = taskCommandService.handle(updateTaskCommand);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResource> updateTaskStatus(
            @PathVariable Long taskId, @RequestBody UpdateTaskStatusResource resource) {
        var updateStatusCommand = UpdateTaskStatusCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        var task = taskCommandService.handle(updateStatusCommand);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskResource> assignUserToTask(
            @PathVariable Long taskId, @RequestBody AssignUserToTaskResource resource) {
        var assignCommand = AssignUserToTaskCommandFromResourceAssembler.toCommandFromResource(taskId, resource);
        var task = taskCommandService.handle(assignCommand);
        if (task.isEmpty()) return ResponseEntity.notFound().build();

        var taskResource = TaskResourceFromEntityAssembler.toResourceFromEntity(task.get());
        return ResponseEntity.ok(taskResource);
    }

    @PutMapping("/{taskId}/unassign")
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
    public ResponseEntity<?> deleteTask(@PathVariable Long taskId) {
        var deleteCommand = new DeleteTaskCommand(taskId);
        try {
            taskCommandService.handle(deleteCommand);
            return ResponseEntity.ok("Task deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
