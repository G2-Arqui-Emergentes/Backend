package pe.edu.upc.managewise.backend.task.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.task.domain.model.commands.UpdateTaskCommand;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.UpdateTaskResource;

public class UpdateTaskCommandFromResourceAssembler {
    public static UpdateTaskCommand toCommandFromResource(Long taskId, UpdateTaskResource resource) {
        return new UpdateTaskCommand(
                taskId,
                resource.title(),
                resource.description(),
                resource.endDate(),
                resource.status() != null ? Status.valueOf(resource.status().toUpperCase()) : null,
                resource.priority() != null ? Priority.valueOf(resource.priority().toUpperCase()) : null,
                resource.assignedUserIds()
        );
    }
}
