package pe.edu.upc.taskmaster.backend.iam.domain.model.commands;

public record UpdateUserCommand(
        String name,
        String lastName,
        String imageUrl,
        Double salary,
        String phone,
        Integer age,
        String bio
) {
    public UpdateUserCommand {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name cannot exceed 50 characters");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
        if (lastName.length() > 50) {
            throw new IllegalArgumentException("Last name cannot exceed 50 characters");
        }
        if (imageUrl != null && imageUrl.length() > 255) {
            throw new IllegalArgumentException("Image URL cannot exceed 255 characters");
        }
        if (salary != null && salary <= 0) {
            throw new IllegalArgumentException("Salary cannot be negative or zero");
        }
        if (phone != null && phone.length() > 20) {
            throw new IllegalArgumentException("Phone cannot exceed 20 characters");
        }
        if (age != null && (age < 18 || age > 100)) {
            throw new IllegalArgumentException("Age must be between 18 and 100");
        }
        if (bio != null && bio.length() > 500) {
            throw new IllegalArgumentException("Bio cannot exceed 500 characters");
        }
    }
}
