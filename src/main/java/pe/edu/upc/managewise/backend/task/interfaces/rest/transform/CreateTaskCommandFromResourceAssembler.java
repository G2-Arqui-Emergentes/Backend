package pe.edu.upc.managewise.backend.task.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.task.domain.model.commands.CreateTaskCommand;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.CreateTaskResource;

public class CreateTaskCommandFromResourceAssembler {
    public static CreateTaskCommand toCommandFromResource(CreateTaskResource resource) {
        return new CreateTaskCommand(
                resource.projectId(),
                resource.title(),
                resource.description(),
                resource.startDate(),
                resource.endDate(),
                Status.valueOf(resource.status().toUpperCase()),
                Priority.valueOf(resource.priority().toUpperCase()),
                resource.assignedUserIds()
        );
    }
}
