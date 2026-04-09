package pe.edu.upc.managewise.backend.notification.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.managewise.backend.notification.domain.model.queries.GetNotificationsByUserIdQuery;
import pe.edu.upc.managewise.backend.notification.domain.services.NotificationQueryService;
import pe.edu.upc.managewise.backend.notification.infrastructure.persistence.jpa.repositories.NotificationRepository;

import java.util.List;

@Service
public class NotificationQueryServiceImpl implements NotificationQueryService {

    private final NotificationRepository notificationRepository;

    public NotificationQueryServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> handle(GetNotificationsByUserIdQuery query) {
        return notificationRepository.findByUserId(query.userId());
    }
}
