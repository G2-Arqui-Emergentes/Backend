package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.ChangePasswordCommand;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.ChangePasswordResource;

public class ChangePasswordCommandFromResourceAssembler {
    public static ChangePasswordCommand toCommandFromResource(
            Long userId,
            ChangePasswordResource resource) {
        return new ChangePasswordCommand(
                userId,
                resource.currentPassword(),
                resource.newPassword()
        );
    }
}
