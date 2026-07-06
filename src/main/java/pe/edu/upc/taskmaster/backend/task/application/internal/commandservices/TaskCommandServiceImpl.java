package pe.edu.upc.taskmaster.backend.task.application.internal.commandservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.taskmaster.backend.project.infrastructure.persistence.jpa.repositories.ProjectRepository;
import pe.edu.upc.taskmaster.backend.meeting.application.external.GoogleCalendarService;
import pe.edu.upc.taskmaster.backend.task.domain.model.aggregates.Task;
import pe.edu.upc.taskmaster.backend.task.domain.model.commands.*;
import pe.edu.upc.taskmaster.backend.task.domain.services.TaskCommandService;
import pe.edu.upc.taskmaster.backend.task.infrastructure.persistence.jpa.repositories.TaskRepository;
import pe.edu.upc.taskmaster.backend.notification.domain.services.NotificationCommandService;
import pe.edu.upc.taskmaster.backend.notification.domain.model.commands.CreateNotificationCommand;

import java.util.Optional;
import java.util.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskCommandServiceImpl implements TaskCommandService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationCommandService notificationCommandService;
    private final GoogleCalendarService googleCalendarService;


    private static final long DUE_SOON_THRESHOLD_HOURS = 24;

    public TaskCommandServiceImpl(TaskRepository taskRepository,
                                  ProjectRepository projectRepository,
                                  UserRepository userRepository,
                                  NotificationCommandService notificationCommandService,
                                  GoogleCalendarService googleCalendarService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationCommandService = notificationCommandService;
        this.googleCalendarService = googleCalendarService;
    }

    @Override
    public Optional<Task> handle(CreateTaskCommand command) {
        var project = projectRepository.findById(command.projectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        var assignedUsers = userRepository.findAllById(command.assignedUserIds());
        if (assignedUsers.size() != command.assignedUserIds().size()) {
            throw new RuntimeException("One or more users not found");
        }

        var task = new Task(
                project,
                command.title(),
                command.description(),
                command.startDate(),
                command.endDate(),
                command.status(),
                command.priority()
        );

        assignedUsers.forEach(task::assignUser);

        var savedTask = taskRepository.save(task);


        try {
            String title = "Nueva tarea asignada";
            for (var user : assignedUsers) {
                String message = String.format("Has sido asignado a la tarea '%s' en el proyecto '%s'", savedTask.getTitle(), project.getName());
                notificationCommandService.handle(new CreateNotificationCommand(user.getId(), title, message));
            }
        } catch (Exception ignored) {

        }


        try {
            notifyIfDueSoon(savedTask);
        } catch (Exception ignored) {
        }

        try {
            syncTaskToGoogleCalendar(savedTask);
        } catch (Exception ignored) {
        }

        return Optional.of(savedTask);
    }

    @Override
    public Optional<Task> handle(UpdateTaskCommand command) {
        var task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Set<Long> previousAssignedUserIds = task.getAssignedUsers().stream()
                .map(user -> user.getId())
                .collect(Collectors.toCollection(HashSet::new));

        task.updateDetails(
                command.title(),
                command.description(),
                command.endDate(),
                command.priority()
        );

        if (command.status() != null) {
            task.updateStatus(command.status());
        }

        if (command.assignedUserIds() != null) {
            task.getAssignedUsers().clear();

            var assignedUsers = userRepository.findAllById(command.assignedUserIds());
            assignedUsers.forEach(task::assignUser);

            try {
                String title = "Tarea actualizada";
                for (var user : assignedUsers) {
                    String message = String.format("Has sido asignado (o mantenido) en la tarea '%s'", task.getTitle());
                    notificationCommandService.handle(new CreateNotificationCommand(user.getId(), title, message));
                }
            } catch (Exception ignored) {
            }
        }

        var updatedTask = taskRepository.save(task);


        try {
            notifyIfDueSoon(updatedTask);
        } catch (Exception ignored) {
        }

        try {
            syncTaskToGoogleCalendar(updatedTask);
        } catch (Exception ignored) {
        }

        if (command.assignedUserIds() != null) {
            Set<Long> currentAssignedUserIds = updatedTask.getAssignedUsers().stream()
                    .map(user -> user.getId())
                    .collect(Collectors.toSet());
            previousAssignedUserIds.stream()
                    .filter(userId -> !currentAssignedUserIds.contains(userId))
                    .forEach(userId -> deleteTaskEventForUser(updatedTask, userId));
        }

        return Optional.of(updatedTask);
    }

    @Override
    public Optional<Task> handle(UpdateTaskStatusCommand command) {
        var task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.updateStatus(command.status());

        var updatedTask = taskRepository.save(task);
        return Optional.of(updatedTask);
    }

    @Override
    public Optional<Task> handle(AssignUserToTaskCommand command) {
        var task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.assignUser(user);

        var updatedTask = taskRepository.save(task);


        try {
            String title = "Asignación de tarea";
            String message = String.format("Has sido asignado a la tarea '%s' en el proyecto '%s'", updatedTask.getTitle(), updatedTask.getProject().getName());
            notificationCommandService.handle(new CreateNotificationCommand(user.getId(), title, message));
        } catch (Exception ignored) {
        }


        try {
            notifyIfDueSoon(updatedTask);
        } catch (Exception ignored) {
        }

        try {
            syncTaskToGoogleCalendar(updatedTask);
        } catch (Exception ignored) {
        }

        return Optional.of(updatedTask);
    }

    @Override
    public Optional<Task> handle(DeleteTaskCommand command) {
        var task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        try {
            deleteTaskGoogleCalendarEvents(task);
        } catch (Exception ignored) {
        }

        taskRepository.delete(task);
        return Optional.of(task);
    }

    @Override
    public void handle(RemoveUserFromTaskCommand command) {
        var task = taskRepository.findById(command.taskId())
                .orElseThrow(() -> new RuntimeException("Task not found"));

        var user = userRepository.findById(command.userId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        task.removeUser(user);
        var updatedTask = taskRepository.save(task);

        deleteTaskEventForUser(updatedTask, user.getId());

        try {
            syncTaskToGoogleCalendar(updatedTask);
        } catch (Exception ignored) {
        }
    }

    private void syncTaskToGoogleCalendar(Task task) {
        if (task.getEndDate() == null || task.getProject() == null) {
            return;
        }

        var leaderId = task.getProject().getLeaderId();
        if (leaderId != null) {
            upsertTaskEventForUser(task, leaderId);
        }

        task.getAssignedUsers().forEach(user -> upsertTaskEventForUser(task, user.getId()));
    }

    private void deleteTaskGoogleCalendarEvents(Task task) {
        if (task.getProject() == null) {
            return;
        }

        var leaderId = task.getProject().getLeaderId();
        if (leaderId != null) {
            deleteTaskEventForUser(task, leaderId);
        }

        task.getAssignedUsers().forEach(user -> deleteTaskEventForUser(task, user.getId()));
    }

    private void upsertTaskEventForUser(Task task, Long userId) {
        if (userId == null || task.getEndDate() == null) {
            return;
        }

        String eventId = buildTaskEventId(task.getId(), userId);
        String title = "Vence: " + task.getTitle();
        String description = "Proyecto: " + task.getProject().getName()
                + "\nTarea: " + task.getTitle()
                + (task.getDescription() != null && !task.getDescription().isBlank()
                ? "\n" + task.getDescription()
                : "");
        googleCalendarService.upsertDeadlineEvent(userId, eventId, title, description, task.getEndDate());
    }

    private void deleteTaskEventForUser(Task task, Long userId) {
        if (userId == null) {
            return;
        }
        googleCalendarService.deleteEvent(userId, buildTaskEventId(task.getId(), userId));
    }

    private String buildTaskEventId(Long taskId, Long userId) {
        return "task-" + taskId + "-user-" + userId;
    }

    private void notifyIfDueSoon(Task task) {
        Date endDate = task.getEndDate();
        if (endDate == null) return;

        Instant now = Instant.now();
        Instant endInstant = endDate.toInstant();
        Duration diff = Duration.between(now, endInstant);
        long hoursLeft = diff.toHours();

        if (hoursLeft >= 0 && hoursLeft <= DUE_SOON_THRESHOLD_HOURS) {
            String title = "Tarea por vencer";
            String messageTemplate = "La tarea '%s' del proyecto '%s' vence en %d hora(s)";


            try {
                for (var user : task.getAssignedUsers()) {
                    String message = String.format(messageTemplate, task.getTitle(), task.getProject().getName(), Math.max(1, hoursLeft));
                    notificationCommandService.handle(new CreateNotificationCommand(user.getId(), title, message));
                }
            } catch (Exception ignored) {
            }


            try {
                Long leaderId = task.getProject().getLeaderId();
                if (leaderId != null) {
                    String message = String.format(messageTemplate, task.getTitle(), task.getProject().getName(), Math.max(1, hoursLeft));
                    notificationCommandService.handle(new CreateNotificationCommand(leaderId, title, message));
                }
            } catch (Exception ignored) {
            }
        }
    }
}
