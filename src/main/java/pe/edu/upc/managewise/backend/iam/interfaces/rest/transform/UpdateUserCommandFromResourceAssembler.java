package pe.edu.upc.managewise.backend.iam.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.iam.domain.model.commands.UpdateUserCommand;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {
    public static UpdateUserCommand toCommandFromResource(UpdateUserResource updateUserResource) {
        return new UpdateUserCommand(
                updateUserResource.name(),
                updateUserResource.lastName(),
                updateUserResource.imageUrl(),
                updateUserResource.salary()
        );
    }
}
