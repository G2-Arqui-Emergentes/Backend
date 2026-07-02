package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources;

public record UpdateUserResource(
        String name,
        String lastName,
        String imageUrl,
        Double salary,
        String phone,
        Integer age,
        String bio
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
        if (phone != null && phone.isBlank()) {
            throw new IllegalArgumentException("Phone must not be blank");
        }
        if (age != null && (age < 18 || age > 100)) {
            throw new IllegalArgumentException("Age must be between 18 and 100");
        }
        if (bio != null && bio.isBlank()) {
            throw new IllegalArgumentException("Bio must not be blank");
        }
    }
}
