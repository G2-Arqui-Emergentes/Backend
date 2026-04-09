package pe.edu.upc.managewise.backend.project.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectCode;
import pe.edu.upc.managewise.backend.project.interfaces.rest.resources.ProjectCodeResource;

public class ProjectCodeResourceFromEntityAssembler {
    public static ProjectCodeResource toResourceFromEntity(ProjectCode entity) {
        return new ProjectCodeResource(
                entity.key(),
                entity.expiration()
        );
    }
}
