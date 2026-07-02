package pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects;

public enum PerformanceLevel {
    HIGH("Alto rendimiento"),
    STABLE("Rendimiento estable"),
    REQUIRES_REVIEW("Requiere revisión");

    private final String description;

    PerformanceLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static PerformanceLevel fromScore(double score) {
        if (score >= 80) return HIGH;
        if (score >= 50) return STABLE;
        return REQUIRES_REVIEW;
    }
}
