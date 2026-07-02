package pe.edu.upc.taskmaster.backend.project.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.project.domain.model.commands.AddUserToProjectCommand;
import pe.edu.upc.taskmaster.backend.project.interfaces.rest.resources.AddUserToProjectResource;

public class AddUserToProjectCommandFromResourceAssembler {
    public static AddUserToProjectCommand toCommandFromResource(AddUserToProjectResource resource) {
        return new AddUserToProjectCommand(
                resource.memberId(),
                resource.code()
        );
    }
}
