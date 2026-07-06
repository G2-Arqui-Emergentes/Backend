package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record UpdateMeetingResource(
        String title,
        String description,
        Date startTime,
        Date endTime,
        String status,
        List<Long> participantIds
) {
    public UpdateMeetingResource {
        if (title != null && title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (title != null && title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (description != null && description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (status != null && status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be blank");
        }
        if (startTime != null && endTime != null && startTime.after(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        if (participantIds != null && participantIds.isEmpty()) {
            throw new IllegalArgumentException("Participant IDs cannot be empty");
        }
    }
}
