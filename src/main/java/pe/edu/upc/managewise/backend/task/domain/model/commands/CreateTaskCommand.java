package pe.edu.upc.managewise.backend.task.domain.model.commands;

import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Priority;
import pe.edu.upc.managewise.backend.task.domain.model.valueobjects.Status;

import java.util.Date;
import java.util.List;

public record CreateTaskCommand(
        Long projectId,
        String title,
        String description,
        Date startDate,
        Date endDate,
        Status status,
        Priority priority,
        List<Long> assignedUserIds
) {
    public CreateTaskCommand {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be empty or negative");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Priority cannot be null");
        }
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
