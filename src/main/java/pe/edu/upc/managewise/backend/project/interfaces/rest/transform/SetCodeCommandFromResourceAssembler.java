package pe.edu.upc.managewise.backend.project.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.project.domain.model.commands.SetCodeCommand;
import pe.edu.upc.managewise.backend.project.interfaces.rest.resources.SetCodeResource;

public class SetCodeCommandFromResourceAssembler {
    public static SetCodeCommand toCommandFromResource(Long projectId, SetCodeResource resource) {
        return new SetCodeCommand(
                projectId,
                resource.keycode(),
                resource.expiration()
        );
    }
}
