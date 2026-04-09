package pe.edu.upc.managewise.backend.notification.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.managewise.backend.notification.interfaces.rest.resources.NotificationResource;

public class NotificationResourceFromEntityAssembler {
    public static NotificationResource toResourceFromEntity(Notification entity) {
        return new NotificationResource(
                entity.getId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getSentAt()
        );
    }
}
