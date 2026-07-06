package pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates.User;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.CreateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.commands.UpdateMeetingCommand;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.valueobjects.MeetingStatus;
import pe.edu.upc.taskmaster.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
public class Meeting extends AuditableAbstractAggregateRoot<Meeting> {

    private Long leaderId;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Column(length = 255)
    private String meetLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> participants = new HashSet<>();

    protected Meeting() {
        super();
    }

    public Meeting(CreateMeetingCommand createMeetingCommand) {
        this();
        this.leaderId = createMeetingCommand.leaderId();
        this.title = createMeetingCommand.title();
        this.description = createMeetingCommand.description();
        this.startTime = createMeetingCommand.startTime();
        this.endTime = createMeetingCommand.endTime();
        this.status = MeetingStatus.SCHEDULED;
        this.meetLink = null;
    }

    public Meeting updateMeeting(UpdateMeetingCommand updateMeetingCommand) {
        if (updateMeetingCommand.title() != null) {
            this.title = updateMeetingCommand.title();
        }
        if (updateMeetingCommand.description() != null) {
            this.description = updateMeetingCommand.description();
        }
        if (updateMeetingCommand.startTime() != null) {
            this.startTime = updateMeetingCommand.startTime();
        }
        if (updateMeetingCommand.endTime() != null) {
            this.endTime = updateMeetingCommand.endTime();
        }
        if (updateMeetingCommand.status() != null) {
            this.status = updateMeetingCommand.status();
        }
        return this;
    }

    public void replaceParticipants(Set<User> participants) {
        this.participants.clear();
        this.participants.addAll(participants);
    }

    public void setMeetLink(String meetLink) {
        this.meetLink = meetLink;
    }
}
