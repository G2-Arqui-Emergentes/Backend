package pe.edu.upc.managewise.backend.task.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record CreateTaskResource(
        Long projectId,
        String title,
        String description,
        Date startDate,
        Date endDate,
        String status,
        String priority,
        List<Long> assignedUserIds
) {
    public CreateTaskResource {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be null or negative");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        if (priority == null || priority.isBlank()) {
            throw new IllegalArgumentException("Priority cannot be null or empty");
        }
        if (assignedUserIds == null) {
            throw new IllegalArgumentException("Assigned user IDs cannot be null");
        }
        if (startDate != null && endDate != null && startDate.after(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }
}
