package pe.edu.upc.taskmaster.backend.meeting.domain.model.commands;

public record DeleteMeetingCommand(Long meetingId) {
    public DeleteMeetingCommand {
        if (meetingId == null || meetingId <= 0) {
            throw new IllegalArgumentException("Meeting ID cannot be null or negative");
        }
    }
}
