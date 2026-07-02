package pe.edu.upc.taskmaster.backend.ai.interfaces.rest.resources;

import java.util.List;

public record MemberDashboardResponseResource(
        WeeklySummaryResource weeklySummary,
        List<String> performanceSuggestions
) {}
