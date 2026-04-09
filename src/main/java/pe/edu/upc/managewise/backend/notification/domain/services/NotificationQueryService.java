package pe.edu.upc.managewise.backend.notification.domain.services;

import pe.edu.upc.managewise.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.managewise.backend.notification.domain.model.queries.GetNotificationsByUserIdQuery;

import java.util.List;

public interface NotificationQueryService {
    List<Notification> handle(GetNotificationsByUserIdQuery query);
}
