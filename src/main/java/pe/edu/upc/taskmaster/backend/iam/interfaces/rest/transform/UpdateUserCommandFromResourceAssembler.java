package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {
    public static UpdateUserCommand toCommandFromResource(UpdateUserResource updateUserResource) {
        return new UpdateUserCommand(
                updateUserResource.name(),
                updateUserResource.lastName(),
                updateUserResource.imageUrl(),
                updateUserResource.salary(),
                updateUserResource.phone(),
                updateUserResource.age(),
                updateUserResource.bio()
        );
    }
}
