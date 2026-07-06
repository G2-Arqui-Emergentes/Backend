package pe.edu.upc.taskmaster.backend.meeting.application.internal.services;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GoogleOAuthStateService {

    private static final Duration TTL = Duration.ofMinutes(10);

    private final Map<String, PendingState> states = new ConcurrentHashMap<>();

    public String createState(Long userId) {
        String state = UUID.randomUUID().toString();
        states.put(state, new PendingState(userId, Instant.now().plus(TTL)));
        return state;
    }

    public Long consumeUserId(String state) {
        if (state == null || state.isBlank()) {
            return null;
        }

        PendingState pendingState = states.remove(state);
        if (pendingState == null) {
            return null;
        }

        if (pendingState.expiresAt().isBefore(Instant.now())) {
            return null;
        }

        return pendingState.userId();
    }

    private record PendingState(Long userId, Instant expiresAt) {}
}
