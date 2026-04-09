package pe.edu.upc.managewise.backend.project.domain.model.commands;

import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectStatus;
import java.util.Date;

public record CreateProjectCommand(
        Long leaderId,
        String name,
        String description,
        String imageUrl,
        Double budget,
        Date endDate
) {
    public CreateProjectCommand{
        if (leaderId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Project description cannot be null or empty");
        }
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (budget == null || budget < 0) {
            throw new IllegalArgumentException("Budget cannot be null or negative");
        }
    }
}
