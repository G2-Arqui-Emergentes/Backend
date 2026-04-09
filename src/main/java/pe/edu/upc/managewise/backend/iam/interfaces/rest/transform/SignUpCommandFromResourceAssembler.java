package pe.edu.upc.managewise.backend.iam.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.iam.domain.model.commands.SignUpCommand;
import pe.edu.upc.managewise.backend.iam.domain.model.entities.Role;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.SignUpResource;

import java.util.ArrayList;

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
