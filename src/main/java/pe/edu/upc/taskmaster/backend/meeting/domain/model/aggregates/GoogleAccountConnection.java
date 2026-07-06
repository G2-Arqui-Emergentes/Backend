package pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upc.taskmaster.backend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "google_connections")
public class GoogleAccountConnection extends AuditableAbstractAggregateRoot<GoogleAccountConnection> {

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 2048)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

    private Instant accessTokenExpiresAt;

    @Column(length = 120)
    private String googleEmail;

    protected GoogleAccountConnection() {
        super();
    }

    public GoogleAccountConnection(Long userId,
                                   String accessToken,
                                   String refreshToken,
                                   Instant accessTokenExpiresAt,
                                   String googleEmail) {
        this();
        this.userId = userId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.accessTokenExpiresAt = accessTokenExpiresAt;
        this.googleEmail = googleEmail;
    }

    public void updateTokens(String accessToken, String refreshToken, Instant accessTokenExpiresAt) {
        this.accessToken = accessToken;
        if (refreshToken != null && !refreshToken.isBlank()) {
            this.refreshToken = refreshToken;
        }
        this.accessTokenExpiresAt = accessTokenExpiresAt;
    }
}
