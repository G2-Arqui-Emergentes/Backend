package pe.edu.upc.managewise.backend.project.interfaces.rest.resources;

import java.util.Date;

public record ProjectCodeResource(
        String key,
        Date expiration
) {
    public ProjectCodeResource {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (expiration == null) {
            throw new IllegalArgumentException("Expiration date cannot be null");
        }
    }
}
