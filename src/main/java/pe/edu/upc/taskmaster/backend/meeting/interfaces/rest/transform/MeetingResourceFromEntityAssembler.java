package pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.transform;

import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.Meeting;
import pe.edu.upc.taskmaster.backend.meeting.interfaces.rest.resources.MeetingResource;

import java.util.stream.Collectors;

public class MeetingResourceFromEntityAssembler {
    public static MeetingResource toResourceFromEntity(Meeting entity) {
        return new MeetingResource(
                entity.getId(),
                entity.getLeaderId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getParticipants().stream()
                        .map(user -> user.getId())
                        .collect(Collectors.toList()),
                entity.getMeetLink(),
                entity.getStatus().name(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
