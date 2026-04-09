package pe.edu.upc.managewise.backend.task.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.managewise.backend.task.domain.model.queries.*;
import pe.edu.upc.managewise.backend.task.domain.services.TaskQueryService;
import pe.edu.upc.managewise.backend.task.infrastructure.persistence.jpa.repositories.TaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskQueryServiceImpl implements TaskQueryService {

    private final TaskRepository taskRepository;

    public TaskQueryServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> handle(GetAllTasksQuery query) {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> handle(GetTaskByIdQuery query) {
        return taskRepository.findById(query.taskId());
    }

    @Override
    public List<Task> handle(GetTasksByProjectIdQuery query) {
        return taskRepository.findByProjectId(query.projectId());
    }

    @Override
    public List<Task> handle(GetTasksByAssignedUserIdQuery query) {
        return taskRepository.findAll().stream()
                .filter(task -> task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(query.userId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> handle(GetTasksByProjectIdAndAssignedUserIdQuery query) {
        return taskRepository.findByProjectId(query.projectId()).stream()
                .filter(task -> task.getAssignedUsers().stream()
                        .anyMatch(user -> user.getId().equals(query.userId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> handle(GetTasksByStatusQuery query) {
        return taskRepository.findByProjectId(query.projectId()).stream()
                .filter(task -> task.getStatus().equals(query.status()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> handle(GetTasksByPriorityQuery query) {
        return taskRepository.findByProjectId(query.projectId()).stream()
                .filter(task -> task.getPriority().equals(query.priority()))
                .collect(Collectors.toList());
    }
}
