package pe.edu.upc.managewise.backend.notification.interfaces.rest.resources;

public record CreateNotificationResource(
        String title,
        String message
) {
    public CreateNotificationResource {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }
}
