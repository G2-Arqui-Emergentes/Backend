package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources;

public record ProjectRiskResource(
        Long id,
        String name,
        String status,
        double delayRisk,
        double overallEfficiency,
        String riskLevel,
        int totalTasks,
        int completedTasks,
        int delayedTasks,
        int inProgressTasks
) {}
