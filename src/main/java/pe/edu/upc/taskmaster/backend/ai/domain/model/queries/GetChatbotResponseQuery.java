package pe.edu.upc.taskmaster.backend.ai.domain.model.queries;

public record GetChatbotResponseQuery(Long userId, String userMessage) {
    public GetChatbotResponseQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        if (userMessage == null || userMessage.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }
}
