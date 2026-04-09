package pe.edu.upc.managewise.backend.project.interfaces.rest.resources;

public record CreateProjectResource(
        String name,
        String description,
        String imageUrl,
        Double budget,
        String endDate
) {
    public CreateProjectResource{
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Project description cannot be null or empty");
        }
        if (endDate == null || endDate.isBlank()) {
            throw new IllegalArgumentException("End date cannot be null or empty");
        }
        if (budget == null || budget < 0) {
            throw new IllegalArgumentException("Budget cannot be null or negative");
        }
    }
}
