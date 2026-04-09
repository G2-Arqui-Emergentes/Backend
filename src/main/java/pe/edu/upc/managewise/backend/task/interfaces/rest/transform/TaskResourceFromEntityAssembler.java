package pe.edu.upc.managewise.backend.task.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.TaskResource;

import java.util.stream.Collectors;

public class TaskResourceFromEntityAssembler {
    public static TaskResource toResourceFromEntity(Task entity) {
        return new TaskResource(
                entity.getId(),
                entity.getProject().getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getStatus().name(),
                entity.getPriority().name(),
                entity.getAssignedUsers().stream()
                        .map(user -> user.getId())
                        .collect(Collectors.toList()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
