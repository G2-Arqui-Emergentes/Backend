package pe.edu.upc.managewise.backend.notification.domain.services;

import pe.edu.upc.managewise.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.CreateNotificationCommand;

import java.util.Optional;

public interface NotificationCommandService {
    Optional<Notification> handle(CreateNotificationCommand command);
}
