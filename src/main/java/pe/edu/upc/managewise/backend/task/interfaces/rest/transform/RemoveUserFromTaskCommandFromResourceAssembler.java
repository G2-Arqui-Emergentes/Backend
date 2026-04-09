package pe.edu.upc.managewise.backend.task.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.task.domain.model.commands.RemoveUserFromTaskCommand;
import pe.edu.upc.managewise.backend.task.interfaces.rest.resources.RemoveUserFromTaskResource;

public class RemoveUserFromTaskCommandFromResourceAssembler {
    public static RemoveUserFromTaskCommand toCommandFromResource(Long taskId, RemoveUserFromTaskResource resource) {
        return new RemoveUserFromTaskCommand(
                taskId,
                resource.userId()
        );
    }
}
