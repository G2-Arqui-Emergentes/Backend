package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources;

import java.util.List;

public record DashboardResponseResource(
        ProjectRiskResource topRiskProject,
        List<String> recommendations,
        List<MemberPerformanceResource> memberPerformances,
        String generatedAt,
        int totalProjects,
        int highRiskProjects
) {}
