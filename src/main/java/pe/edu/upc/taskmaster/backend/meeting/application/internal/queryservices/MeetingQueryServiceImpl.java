package pe.edu.upc.taskmaster.backend.meeting.application.internal.queryservices;

import org.springframework.stereotype.Service;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.Meeting;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.queries.GetAllMeetingsQuery;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.queries.GetMeetingByIdQuery;
import pe.edu.upc.taskmaster.backend.meeting.domain.services.MeetingQueryService;
import pe.edu.upc.taskmaster.backend.meeting.infrastructure.persistence.jpa.repositories.MeetingRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MeetingQueryServiceImpl implements MeetingQueryService {

    private final MeetingRepository meetingRepository;

    public MeetingQueryServiceImpl(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    @Override
    public List<Meeting> handle(GetAllMeetingsQuery getAllMeetingsQuery) {
        return meetingRepository.findAll();
    }

    @Override
    public Optional<Meeting> handle(GetMeetingByIdQuery getMeetingByIdQuery) {
        return meetingRepository.findById(getMeetingByIdQuery.meetingId());
    }
}
