package pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates;

import lombok.Getter;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.AIRecommendation;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.RiskLevel;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class AIInsight {
    private final Long projectId;
    private final String projectName;
    private final double delayRisk;
    private final double overallEfficiency;
    private final RiskLevel riskLevel;
    private final int totalTasks;
    private final int completedTasks;
    private final int delayedTasks;
    private final int inProgressTasks;
    private final List<AIRecommendation> recommendations;
    private final LocalDateTime generatedAt;

    private AIInsight(Builder builder) {
        this.projectId = builder.projectId;
        this.projectName = builder.projectName;
        this.delayRisk = builder.delayRisk;
        this.overallEfficiency = builder.overallEfficiency;
        this.riskLevel = builder.riskLevel;
        this.totalTasks = builder.totalTasks;
        this.completedTasks = builder.completedTasks;
        this.delayedTasks = builder.delayedTasks;
        this.inProgressTasks = builder.inProgressTasks;
        this.recommendations = builder.recommendations;
        this.generatedAt = builder.generatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long projectId;
        private String projectName;
        private double delayRisk;
        private double overallEfficiency;
        private RiskLevel riskLevel;
        private int totalTasks;
        private int completedTasks;
        private int delayedTasks;
        private int inProgressTasks;
        private List<AIRecommendation> recommendations;
        private LocalDateTime generatedAt;

        public Builder projectId(Long projectId) { this.projectId = projectId; return this; }
        public Builder projectName(String projectName) { this.projectName = projectName; return this; }
        public Builder delayRisk(double delayRisk) { this.delayRisk = delayRisk; return this; }
        public Builder overallEfficiency(double overallEfficiency) { this.overallEfficiency = overallEfficiency; return this; }
        public Builder riskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; return this; }
        public Builder totalTasks(int totalTasks) { this.totalTasks = totalTasks; return this; }
        public Builder completedTasks(int completedTasks) { this.completedTasks = completedTasks; return this; }
        public Builder delayedTasks(int delayedTasks) { this.delayedTasks = delayedTasks; return this; }
        public Builder inProgressTasks(int inProgressTasks) { this.inProgressTasks = inProgressTasks; return this; }
        public Builder recommendations(List<AIRecommendation> recommendations) { this.recommendations = recommendations; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }
        public AIInsight build() { return new AIInsight(this); }
    }
}
