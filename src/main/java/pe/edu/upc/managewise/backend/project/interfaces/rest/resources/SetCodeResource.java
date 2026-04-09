package pe.edu.upc.managewise.backend.project.interfaces.rest.resources;

import java.util.Date;

public record SetCodeResource(
        String keycode,
        Date expiration
) {
    public SetCodeResource{
        if (keycode == null || keycode.isBlank()) {
            throw new IllegalArgumentException("Keycode cannot be null or empty");
        }
        if (expiration == null || expiration.before(new Date())) {
            throw new IllegalArgumentException("Expiration date cannot be null or in the past");
        }
    }
}
