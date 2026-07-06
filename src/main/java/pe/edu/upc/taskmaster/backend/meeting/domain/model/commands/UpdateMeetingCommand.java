package pe.edu.upc.taskmaster.backend.meeting.domain.model.commands;

import pe.edu.upc.taskmaster.backend.meeting.domain.model.valueobjects.MeetingStatus;

import java.util.Date;
import java.util.List;

public record UpdateMeetingCommand(
        Long meetingId,
        String title,
        String description,
        Date startTime,
        Date endTime,
        MeetingStatus status,
        List<Long> participantIds
) {
    public UpdateMeetingCommand {
        if (meetingId == null || meetingId <= 0) {
            throw new IllegalArgumentException("Meeting ID cannot be null or negative");
        }
        if (title != null && title.length() > 100) {
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        if (title != null && title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be blank");
        }
        if (description != null && description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }
        if (startTime != null && endTime != null && startTime.after(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        if (participantIds != null && participantIds.isEmpty()) {
            throw new IllegalArgumentException("Participant IDs cannot be empty");
        }
    }
}
