package pe.edu.upc.managewise.backend.iam.interfaces.rest.resources;

public record UpdateUserResource(
        String name,
        String lastName,
        String imageUrl,
        Double salary
) {
    public UpdateUserResource {
        if (name != null && name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (lastName != null && lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be blank");
        }
        if (imageUrl != null && imageUrl.isBlank()) {
            throw new IllegalArgumentException("Image URL must not be blank");
        }
        if (salary != null && salary < 0) {
            throw new IllegalArgumentException("Salary must not be negative");
        }
    }
}
