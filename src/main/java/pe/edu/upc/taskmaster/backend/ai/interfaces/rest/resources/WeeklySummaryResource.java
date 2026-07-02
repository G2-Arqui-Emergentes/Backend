package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources;

public record WeeklySummaryResource(
        double smartVelocity,
        String summary,
        String riskPrediction,
        String generatedAt
) {}
