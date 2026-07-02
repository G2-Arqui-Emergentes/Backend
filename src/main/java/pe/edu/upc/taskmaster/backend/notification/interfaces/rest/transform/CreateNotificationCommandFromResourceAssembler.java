package pe.edu.upc.taskmaster.backend.notification.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.notification.domain.model.commands.CreateNotificationCommand;
import pe.edu.upc.taskmaster.backend.notification.interfaces.rest.resources.CreateNotificationResource;

public class CreateNotificationCommandFromResourceAssembler {
    public static CreateNotificationCommand toCommandFromResource(CreateNotificationResource resource, Long userId) {
        return new CreateNotificationCommand(
                userId,
                resource.title(),
                resource.message()
        );
    }
}
