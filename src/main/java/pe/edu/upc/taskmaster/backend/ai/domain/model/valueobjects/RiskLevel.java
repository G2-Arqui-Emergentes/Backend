package pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects;

public enum RiskLevel {
    HIGH("Alto"),
    MEDIUM("Medio"),
    LOW("Bajo");

    private final String description;

    RiskLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static RiskLevel fromDelayRisk(double delayRisk) {
        if (delayRisk > 70) return HIGH;
        if (delayRisk > 40) return MEDIUM;
        return LOW;
    }
}
