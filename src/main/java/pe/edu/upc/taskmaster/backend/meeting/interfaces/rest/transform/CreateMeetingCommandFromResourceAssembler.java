package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.CreateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources.CreateMeetingResource;

public class CreateMeetingCommandFromResourceAssembler {
    public static CreateMeetingCommand toCommandFromResource(CreateMeetingResource resource, Long leaderId) {
        return new CreateMeetingCommand(
                leaderId,
                resource.title(),
                resource.description(),
                resource.startTime(),
                resource.endTime(),
                resource.participantIds()
        );
    }
}
