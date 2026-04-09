package pe.edu.upc.managewise.backend.task.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record UpdateTaskResource(
        String title,
        String description,
        Date endDate,
        String status,
        String priority,
        List<Long> assignedUserIds
) {
    public UpdateTaskResource {
        if (title != null && title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
    }
}
