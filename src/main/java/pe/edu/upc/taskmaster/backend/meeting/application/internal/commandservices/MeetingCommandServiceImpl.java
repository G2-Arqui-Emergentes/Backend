package pe.edu.upc.taskmaster.backend.meeting.application.internal.commandservices;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pe.edu.upc.taskmaster.backend.iam.domain.model.valueobjects.Roles;
import pe.edu.upc.taskmaster.backend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.Meeting;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.CreateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.DeleteMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.UpdateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.services.MeetingCommandService;
import pe.edu.upc.taskmaster.backend.meeting.infrastructure.persistence.jpa.repositories.MeetingRepository;
import pe.edu.upc.taskmaster.backend.notification.domain.model.commands.CreateNotificationCommand;
import pe.edu.upc.taskmaster.backend.notification.domain.services.NotificationCommandService;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

@Service
public class MeetingCommandServiceImpl implements MeetingCommandService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final NotificationCommandService notificationCommandService;

    public MeetingCommandServiceImpl(MeetingRepository meetingRepository,
                                     UserRepository userRepository,
                                     NotificationCommandService notificationCommandService) {
        this.meetingRepository = meetingRepository;
        this.userRepository = userRepository;
        this.notificationCommandService = notificationCommandService;
    }

    @Override
    @Transactional
    public Optional<Meeting> handle(CreateMeetingCommand createMeetingCommand) {
        var leaderOptional = userRepository.findById(createMeetingCommand.leaderId());
        if (leaderOptional.isEmpty()) {
            throw new IllegalArgumentException("Leader with ID " + createMeetingCommand.leaderId() + " not found");
        }

        var leader = leaderOptional.get();
        boolean isLeader = leader.getRoles().stream()
                .anyMatch(role -> role.getName() == Roles.ROLE_LEADER);

        if (!isLeader) {
            throw new IllegalArgumentException("Only leaders can create meetings");
        }

        var participantIds = createMeetingCommand.participantIds().stream()
                .distinct()
                .toList();

        var participants = userRepository.findAllById(participantIds);
        if (participants.size() != participantIds.size()) {
            throw new IllegalArgumentException("One or more participants were not found");
        }

        var meeting = new Meeting(createMeetingCommand);
        meeting.replaceParticipants(new HashSet<>(participants));

        var savedMeeting = meetingRepository.save(meeting);

        try {
            String title = "Nueva reunion";
            String leaderName = leader.getName() != null ? leader.getName() : leader.getEmail();
            for (var participant : participants) {
                String message = String.format("%s te ha invitado a la reunion '%s'", leaderName, savedMeeting.getTitle());
                notificationCommandService.handle(new CreateNotificationCommand(participant.getId(), title, message));
            }
        } catch (Exception ignored) {
        }

        return Optional.of(savedMeeting);
    }

    @Override
    @Transactional
    public Optional<Meeting> handle(UpdateMeetingCommand updateMeetingCommand, Long leaderId) {
        var meetingOptional = meetingRepository.findById(updateMeetingCommand.meetingId());
        if (meetingOptional.isEmpty()) {
            throw new IllegalArgumentException("Meeting with ID " + updateMeetingCommand.meetingId() + " not found");
        }

        var meeting = meetingOptional.get();
        if (!meeting.getLeaderId().equals(leaderId)) {
            throw new IllegalArgumentException("Leader with ID " + leaderId + " is not the owner of this meeting");
        }

        meeting.updateMeeting(updateMeetingCommand);

        if (updateMeetingCommand.participantIds() != null) {
            var participantIds = new LinkedHashSet<>(updateMeetingCommand.participantIds());
            var participants = userRepository.findAllById(participantIds);
            if (participants.size() != participantIds.size()) {
                throw new IllegalArgumentException("One or more participants were not found");
            }
            meeting.replaceParticipants(new HashSet<>(participants));
        }

        var updatedMeeting = meetingRepository.save(meeting);
        return Optional.of(updatedMeeting);
    }

    @Override
    @Transactional
    public void handle(DeleteMeetingCommand deleteMeetingCommand, Long leaderId) {
        var meetingOptional = meetingRepository.findById(deleteMeetingCommand.meetingId());
        if (meetingOptional.isEmpty()) {
            throw new IllegalArgumentException("Meeting with ID " + deleteMeetingCommand.meetingId() + " not found");
        }

        var meeting = meetingOptional.get();
        if (!meeting.getLeaderId().equals(leaderId)) {
            throw new IllegalArgumentException("Leader with ID " + leaderId + " is not the owner of this meeting");
        }

        meetingRepository.delete(meeting);
    }
}
