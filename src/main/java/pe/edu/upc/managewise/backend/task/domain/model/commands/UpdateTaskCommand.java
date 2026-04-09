package pe.edu.upc.managewise.backend.task.domain.model.commands;

import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;

import java.util.Date;
import java.util.List;

public record UpdateTaskCommand(
        Long taskId,
        String title,
        String description,
        Date endDate,
        Status status,
        Priority priority,
        List<Long> assignedUserIds
) {
    public UpdateTaskCommand {
        if (taskId == null || taskId <= 0) {
            throw new IllegalArgumentException("Task ID cannot be empty or negative");
        }
        if (title != null && title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
    }
}
