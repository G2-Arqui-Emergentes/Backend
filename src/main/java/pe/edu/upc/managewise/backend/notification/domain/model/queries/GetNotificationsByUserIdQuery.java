package pe.edu.upc.managewise.backend.notification.domain.model.queries;

public record GetNotificationsByUserIdQuery(
        Long userId
) {
    public GetNotificationsByUserIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
    }
}
