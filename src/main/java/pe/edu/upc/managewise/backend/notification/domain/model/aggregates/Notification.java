package pe.edu.upc.managewise.backend.notification.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.CreateNotificationCommand;
import pe.edu.upc.managewise.backend.notification.domain.model.commands.UpdateNotificationCommand;
import pe.edu.upc.managewise.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.util.Date;

@Getter
@Entity
public class Notification extends AuditableAbstractAggregateRoot<Notification> {

    private Long userId;
    private String title;
    private String message;
    private Date sentAt;

    protected Notification() {

        super();
    }

    public Notification(Long id, String title, String message, Date date) {

        this.userId = id;
        this.title = title;
        this.message = message;
        this.sentAt = date;
    }

    public Notification(CreateNotificationCommand createNotificationCommand) {
        this.userId = createNotificationCommand.userId();
        this.title = createNotificationCommand.title();
        this.message = createNotificationCommand.message();
        this.sentAt = new Date();
    }

    public Notification updateNotification(UpdateNotificationCommand updateNotificationCommand) {
        this.title = updateNotificationCommand.title();
        this.message = updateNotificationCommand.message();
        this.sentAt = updateNotificationCommand.sentAt();
        return this;
    }
}
