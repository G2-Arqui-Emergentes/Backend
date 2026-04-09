package pe.edu.upc.managewise.backend.notification.interfaces.rest.resources;

import java.util.Date;

public record NotificationResource(
        Long id,
        Long userId,
        String title,
        String message,
        Date sentAt
) {}
