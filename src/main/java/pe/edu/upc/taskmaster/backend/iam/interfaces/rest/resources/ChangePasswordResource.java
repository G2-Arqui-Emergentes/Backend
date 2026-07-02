package pe.edu.upc.taskmaster.backend.iam.interfaces.rest.resources;

public record ChangePasswordResource(
        String currentPassword,
        String newPassword
) {
    public ChangePasswordResource {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password cannot be empty");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be empty");
        }
        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters long");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password must be different from current password");
        }
    }
}