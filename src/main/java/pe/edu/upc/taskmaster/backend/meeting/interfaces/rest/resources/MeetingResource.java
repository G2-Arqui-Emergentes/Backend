package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources;

import java.util.Date;
import java.util.List;

public record MeetingResource(
        Long meetingId,
        Long leaderId,
        String title,
        String description,
        Date startTime,
        Date endTime,
        List<Long> participantIds,
        String meetLink,
        String status,
        Date createdAt,
        Date updatedAt
) {
    public MeetingResource {
        if (meetingId == null || meetingId <= 0) {
            throw new IllegalArgumentException("Meeting ID cannot be null or negative");
        }
        if (leaderId == null || leaderId <= 0) {
            throw new IllegalArgumentException("Leader ID cannot be null or negative");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
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
        if (participantIds == null) {
            throw new IllegalArgumentException("Participant IDs cannot be null");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
    }
}
