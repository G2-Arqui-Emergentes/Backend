package pe.edu.upc.managewise.backend.task.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.task.domain.model.commands.UpdateTaskStatusCommand;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.UpdateTaskStatusResource;

public class UpdateTaskStatusCommandFromResourceAssembler {
    public static UpdateTaskStatusCommand toCommandFromResource(Long taskId, UpdateTaskStatusResource resource) {
        return new UpdateTaskStatusCommand(
                taskId,
                Status.valueOf(resource.status().toUpperCase())
        );
    }
}
