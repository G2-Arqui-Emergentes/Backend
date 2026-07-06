package pe.edu.upc.taskmaster.backend.meeting.domain.services;

import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.Meeting;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.CreateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.DeleteMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.UpdateMeetingCommand;

import java.util.Optional;

public interface MeetingCommandService {
    Optional<Meeting> handle(CreateMeetingCommand createMeetingCommand);
    Optional<Meeting> handle(UpdateMeetingCommand updateMeetingCommand, Long leaderId);
    void handle(DeleteMeetingCommand deleteMeetingCommand, Long leaderId);
}
