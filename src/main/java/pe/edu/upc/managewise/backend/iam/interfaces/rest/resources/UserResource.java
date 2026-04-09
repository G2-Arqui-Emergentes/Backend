package pe.edu.upc.managewise.backend.iam.interfaces.rest.resources;

import pe.edu.upc.managewise.backend.iam.domain.model.valueobjects.Roles;

import java.util.List;

public record UserResource(
        Long id,
        String email,
        List<Roles> roles,
        String name,
        String lastName,
        String imageUrl,
        Double salary,
        List<Long> projectIds
) {
}
