package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources.SignUpResource;

public class SignUpCommandFromResourceAssembler {

    public static SignUpCommand toCommandFromResource(SignUpResource signUpResource) {
        return new SignUpCommand(
                signUpResource.email(),
                signUpResource.password(),
                signUpResource.roles(),
                signUpResource.name(),
                signUpResource.lastName()
        );
    }
}
