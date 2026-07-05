package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources;

public record ChatMessageRequest(String message) {
    public ChatMessageRequest {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }
}
