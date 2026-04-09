package pe.edu.upc.managewise.backend.iam.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.iam.domain.model.commands.SignInCommand;
import pe.edu.upc.managewise.backend.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {

  public static SignInCommand toCommandFromResource(SignInResource signInResource) {
    return new SignInCommand(
            signInResource.email(),
            signInResource.password());
  }
}
