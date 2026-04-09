package pe.edu.upc.managewise.backend.notification.infrastructure.schedulling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.managewise.backend.task.infrastructure.persistence.jpa.repositories.TaskRepository;
import pe.edu.upc.managewise.backend.notification.domain.services.NotificationCommandService;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.CreateNotificationCommand;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class DueDateNotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(DueDateNotificationScheduler.class);

    private final TaskRepository taskRepository;
    private final NotificationCommandService notificationCommandService;


    private static final long DUE_SOON_THRESHOLD_HOURS = 24;

    public DueDateNotificationScheduler(TaskRepository taskRepository,
                                        NotificationCommandService notificationCommandService) {
        this.taskRepository = taskRepository;
        this.notificationCommandService = notificationCommandService;
    }


    @Scheduled(cron = "0 0 * * * *")
    public void checkTasksDueSoon() {
        try {
            Instant now = Instant.now();
            Instant later = now.plus(Duration.ofHours(DUE_SOON_THRESHOLD_HOURS));

            Date from = Date.from(now);
            Date to = Date.from(later);

            List<Task> tasks = taskRepository.findByEndDateBetween(from, to);
            log.info("Found {} tasks with endDate between {} and {}", tasks.size(), from, to);

            for (Task task : tasks) {

                if (task.getStatus() == null) continue;
                if (task.getStatus().name().equals("DONE")) continue;

                long hoursLeft = Duration.between(Instant.now(), task.getEndDate().toInstant()).toHours();
                long displayHours = Math.max(1, hoursLeft);

                String title = "Tarea por vencer";
                String message = String.format("La tarea '%s' del proyecto '%s' vence en %d hora(s)", task.getTitle(), task.getProject().getName(), displayHours);

                try {
                    for (var user : task.getAssignedUsers()) {
                        notificationCommandService.handle(new CreateNotificationCommand(user.getId(), title, message));
                    }
                } catch (Exception e) {
                    log.error("Error notifying assigned users for task {}: {}", task.getId(), e.getMessage());
                }


                try {
                    Long leaderId = task.getProject().getLeaderId();
                    if (leaderId != null) {
                        notificationCommandService.handle(new CreateNotificationCommand(leaderId, title, message));
                    }
                } catch (Exception e) {
                    log.error("Error notifying project leader for task {}: {}", task.getId(), e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error running DueDateNotificationScheduler: {}", e.getMessage());
        }
    }
}
