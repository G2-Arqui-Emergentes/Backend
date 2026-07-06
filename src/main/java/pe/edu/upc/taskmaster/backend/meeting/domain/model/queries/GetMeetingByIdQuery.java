package pe.edu.upc.taskmaster.backend.meeting.domain.model.queries;

public record GetMeetingByIdQuery(Long meetingId) {
    public GetMeetingByIdQuery {
        if (meetingId == null || meetingId <= 0) {
            throw new IllegalArgumentException("Meeting ID cannot be null or negative");
        }
    }
}
