package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record CreateMeetingResource(
        String title,
        String description,
        Date startTime,
        Date endTime,
        List<Long> participantIds
) {
    public CreateMeetingResource {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        if (startTime == null) {
            throw new IllegalArgumentException("Start time cannot be null");
        }
        if (endTime == null) {
            throw new IllegalArgumentException("End time cannot be null");
        }
        if (startTime.after(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("Participant IDs cannot be null or empty");
        }
    }
}
