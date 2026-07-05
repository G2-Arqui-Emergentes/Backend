package pe.edu.upc.taskmaster.backend.ai.domain.services;

import pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates.AIInsight;
import pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates.MemberWeeklySummary;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetChatbotResponseQuery;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetLeaderDashboardQuery;
import pe.edu.upc.taskmaster.backend.ai.domain.model.queries.GetMemberDashboardQuery;

import java.util.List;

public interface AIQueryService {
    List<AIInsight> handle(GetLeaderDashboardQuery query);

    MemberWeeklySummary handle(GetMemberDashboardQuery query);

    String handle(GetChatbotResponseQuery query);
}
