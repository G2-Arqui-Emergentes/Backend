package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources;

public record MemberPerformanceResource(
        Long userId,
        String userName,
        String performanceLevel,
        double score,
        int tasksCompleted,
        int tasksDelayed
) {}
