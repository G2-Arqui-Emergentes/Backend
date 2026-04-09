package pe.edu.upc.managewise.backend.notification.domain.model.commands;

public record CreateNotificationCommand(
        Long userId,
        String title,
        String message
) {
    public CreateNotificationCommand {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
    }
}
