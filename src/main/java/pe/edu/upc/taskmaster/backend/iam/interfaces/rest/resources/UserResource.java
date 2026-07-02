package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources;

import pe.edu.upc.taskmaster.backend.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.taskmaster.backend.iam.domain.model.valueobjects.UserStatus;

import java.util.List;

public record UserResource(
        Long id,
        String email,
        List<Roles> roles,
        String name,
        String lastName,
        String imageUrl,
        Double salary,
        String phone,
        Integer age,
        String bio,
        UserStatus status,
        String lastActivity,
        List<Long> projectIds
) {
}
