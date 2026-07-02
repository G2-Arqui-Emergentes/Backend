package pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects;

public record AIRecommendation(String text, String type, int priority) {
    public AIRecommendation {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Recommendation text cannot be null or empty");
        }
    }
}
