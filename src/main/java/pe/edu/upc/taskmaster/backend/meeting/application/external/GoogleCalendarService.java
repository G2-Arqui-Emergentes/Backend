package pe.edu.upc.taskmaster.backend.meeting.application.external;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import org.springframework.stereotype.Service;
import pe.edu.upc.taskmaster.backend.meeting.application.internal.services.GoogleAccountConnectionService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "taskmaster-backend";

    private final GoogleAccountConnectionService googleAccountConnectionService;

    public GoogleCalendarService(GoogleAccountConnectionService googleAccountConnectionService) {
        this.googleAccountConnectionService = googleAccountConnectionService;
    }

    public String createMeetLink(Long userId,
                                 String title,
                                 String description,
                                 Date startTime,
                                 Date endTime,
                                 List<String> attendeeEmails) {
        var accessToken = googleAccountConnectionService.getValidAccessTokenForUserId(userId);
        return createMeetLinkWithAccessToken(accessToken, title, description, startTime, endTime, attendeeEmails);
    }

    private String createMeetLinkWithAccessToken(String accessToken,
                                 String title,
                                 String description,
                                 Date startTime,
                                 Date endTime,
                                 List<String> attendeeEmails) {
        var calendarService = buildCalendarService(accessToken);

        try {
            var event = new Event()
                    .setSummary(title)
                    .setDescription(description)
                    .setStart(toEventDateTime(startTime))
                    .setEnd(toEventDateTime(endTime))
                    .setAttendees(attendeeEmails.stream()
                            .filter(Objects::nonNull)
                            .distinct()
                            .map(email -> new EventAttendee().setEmail(email))
                            .toList());

            event.setConferenceData(new ConferenceData().setCreateRequest(
                    new CreateConferenceRequest()
                            .setRequestId(UUID.randomUUID().toString())
                            .setConferenceSolutionKey(
                                    new ConferenceSolutionKey().setType("hangoutsMeet")
                            )
            ));

            var createdEvent = calendarService.events()
                    .insert("primary", event)
                    .setConferenceDataVersion(1)
                    .setSendUpdates("all")
                    .execute();

            var meetLink = createdEvent.getHangoutLink();
            if (meetLink == null && createdEvent.getConferenceData() != null && createdEvent.getConferenceData().getEntryPoints() != null) {
                meetLink = createdEvent.getConferenceData().getEntryPoints().stream()
                        .filter(entryPoint -> "video".equals(entryPoint.getEntryPointType()))
                        .map(entryPoint -> entryPoint.getUri())
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
            }

            if (meetLink == null || meetLink.isBlank()) {
                throw new IllegalStateException("Google Meet link was not generated");
            }

            return meetLink;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create Google Calendar event", e);
        }
    }

    private Calendar buildCalendarService(String accessToken) {
        try {
            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                    .setAccessToken(accessToken);

            return new Calendar.Builder(httpTransport, GsonFactory.getDefaultInstance(), credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to initialize Google Calendar client", e);
        }
    }

    private EventDateTime toEventDateTime(Date date) {
        return new EventDateTime()
                .setDateTime(new DateTime(date, TimeZone.getDefault()));
    }
}
