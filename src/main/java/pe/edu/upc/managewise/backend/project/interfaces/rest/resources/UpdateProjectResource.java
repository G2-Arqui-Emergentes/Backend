package pe.edu.upc.managewise.backend.project.interfaces.rest.resources;

public record UpdateProjectResource(
        String name,
        String description,
        String imageUrl,
        Double budget,
        String status,
        String endDate
) {
    public UpdateProjectResource{
        if (name==null) {
            throw new IllegalArgumentException("Project name cannot be null");
        }
        if (description==null) {
            throw new IllegalArgumentException("Project description cannot be null");
        }
        if (budget==null || budget < 0) {
            throw new IllegalArgumentException("Budget cannot be null or negative");
        }
        if (status==null) {
            throw new IllegalArgumentException("Project status cannot be null");
        }
        if (endDate==null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
    }
}
