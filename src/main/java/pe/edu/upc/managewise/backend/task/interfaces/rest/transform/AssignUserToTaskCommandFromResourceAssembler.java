package pe.edu.upc.managewise.backend.task.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.task.domain.model.commands.AssignUserToTaskCommand;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.AssignUserToTaskResource;

public class AssignUserToTaskCommandFromResourceAssembler {
    public static AssignUserToTaskCommand toCommandFromResource(Long taskId, AssignUserToTaskResource resource) {
        return new AssignUserToTaskCommand(
                taskId,
                resource.userId()
        );
    }
}
