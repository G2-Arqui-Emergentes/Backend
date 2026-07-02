package pe.edu.upc.taskmaster.backend.notification.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import pe.edu.upc.taskmaster.backend.notification.domain.model.aggregates.Notification;
import pe.edu.upc.taskmaster.backend.notification.domain.model.queries.GetNotificationsByUserIdQuery;
import pe.edu.upc.taskmaster.backend.notification.domain.services.NotificationCommandService;
import pe.edu.upc.taskmaster.backend.notification.domain.services.NotificationQueryService;
import pe.edu.upc.taskmaster.backend.notification.interfaces.rest.resources.NotificationResource;
import pe.edu.upc.taskmaster.backend.notification.interfaces.rest.transform.NotificationResourceFromEntityAssembler;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Notifications", description = "Notification Management Endpoints")
public class NotificationController {

    private final NotificationCommandService notificationCommandService;
    private final NotificationQueryService notificationQueryService;

    public NotificationController(NotificationCommandService notificationCommandService,
                                  NotificationQueryService notificationQueryService) {
        this.notificationCommandService = notificationCommandService;
        this.notificationQueryService = notificationQueryService;
    }

    @GetMapping("/me")
    @Operation(
            summary = "Get my notifications",
            description = "Retrieves all notifications for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificationResource.class))
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required")
    })
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
