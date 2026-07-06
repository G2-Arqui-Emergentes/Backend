package pe.edu.upc.taskmaster.backend.meeting.domain.services;

import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.Meeting;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.queries.GetAllMeetingsQuery;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.queries.GetMeetingByIdQuery;

import java.util.List;
import java.util.Optional;

public interface MeetingQueryService {
    List<Meeting> handle(GetAllMeetingsQuery getAllMeetingsQuery);
    Optional<Meeting> handle(GetMeetingByIdQuery getMeetingByIdQuery);
}
