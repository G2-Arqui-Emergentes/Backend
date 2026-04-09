package pe.edu.upc.managewise.backend.notification.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;
import pe.edu.upc.managewise.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.managewise.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.CreateNotificationCommand;
import pe.edu.upc.managewise.backend.notification.domain.model.queries.GetNotificationsByUserIdQuery;
import pe.edu.upc.managewise.backend.notification.domain.services.NotificationCommandService;
import pe.edu.upc.managewise.backend.notification.domain.services.NotificationQueryService;
import pe.edu.upc.managewise.backend.notification.interfaces.rest.resources.CreateNotificationResource;
import pe.edu.upc.managewise.backend.notification.interfaces.rest.resources.NotificationResource;
import pe.edu.upc.managewise.backend.notification.interfaces.rest.transform.CreateNotificationCommandFromResourceAssembler;
import pe.edu.upc.managewise.backend.notification.interfaces.rest.transform.NotificationResourceFromEntityAssembler;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = "application/json")
public class NotificationController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;

    public NotificationController(NotificationCommandService notificationCommandService,
                                  NotificationQueryService notificationQueryService) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
    }


    @GetMapping("/me")
    public ResponseEntity<List<NotificationResource>> getMyNotifications() {
        Long userId = getAuthenticatedUserId();
        var query = new GetNotificationsByUserIdQuery(userId);
        List<Notification> notifications = notificationQueryService.handle(query);

        var resources = notifications.stream()
                .map(NotificationResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resources);
    }

    private Long getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userDetails.getId();
        }
        throw new RuntimeException("Invalid principal type");
    }
}
