package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.UpdateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.valueobjects.MeetingStatus;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources.UpdateMeetingResource;

public class UpdateMeetingCommandFromResourceAssembler {
    public static UpdateMeetingCommand toCommandFromResource(Long meetingId, UpdateMeetingResource resource) {
        return new UpdateMeetingCommand(
                meetingId,
                resource.title(),
                resource.description(),
                resource.startTime(),
                resource.endTime(),
                resource.status() != null ? MeetingStatus.valueOf(resource.status().toUpperCase()) : null,
                resource.participantIds()
        );
    }
}
