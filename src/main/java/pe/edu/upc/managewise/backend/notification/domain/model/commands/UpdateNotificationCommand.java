package pe.edu.upc.managewise.backend.notification.domain.model.commands;

import java.util.Date;

public record UpdateNotificationCommand(
        Long notificationId,
        String title,
        String message,
        Date sentAt
) {
    public UpdateNotificationCommand {
        if (notificationId == null || notificationId <= 0) {
            throw new IllegalArgumentException("Notification ID cannot be null or negative");
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
