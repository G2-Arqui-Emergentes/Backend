package pe.edu.upc.managewise.backend.project.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

import java.util.Date;

@Embeddable
public record ProjectCode(String key, Date expiration) {
    public ProjectCode {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
    }
}