package pe.edu.upc.managewise.backend.notification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.managewise.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.CreateNotificationCommand;
import pe.edu.upc.managewise.backend.notification.domain.services.NotificationCommandService;
import pe.edu.upc.managewise.backend.notification.infrastructure.persistence.jpa.repositories.NotificationRepository;
import pe.edu.upc.managewise.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class NotificationCommandServiceImpl implements NotificationCommandService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationCommandServiceImpl(NotificationRepository notificationRepository,
                                          UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Notification> handle(CreateNotificationCommand command) {
        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));


        var notification = new Notification(
                user.getId(),
                command.title(),
                command.message(),
                new Date()
        );

        var savedNotification = notificationRepository.save(notification);

        System.out.println("[Notification] Guardada notification id=" + savedNotification.getId() + ", userId=" + savedNotification.getUserId() + ", title=" + savedNotification.getTitle());
        return Optional.of(savedNotification);
    }
}
