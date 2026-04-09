package pe.edu.upc.managewise.backend.project.interfaces.rest.resources;

public record ProjectResource(
        Long projectId,
        String key,
        Long leaderId,
        String name,
        String description,
        String imageUrl,
        Double budget,
        String status,
        String startDate,
        String endDate
) {
    public ProjectResource {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Project key cannot be null or empty");
        }
        if (leaderId == null || leaderId <= 0) {
            throw new IllegalArgumentException("Leader ID cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Project name cannot be null or empty");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Project description cannot be null or empty");
        }
        if (budget == null || budget <= 0) {
            throw new IllegalArgumentException("Budget cannot be null or negative");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (startDate == null || startDate.isBlank()) {
            throw new IllegalArgumentException("Start date cannot be null or empty");
        }
        if (endDate == null || endDate.isBlank()) {
            throw new IllegalArgumentException("End date cannot be null or empty");
        }
    }
}
