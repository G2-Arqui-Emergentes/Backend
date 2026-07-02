package pe.edu.upc.taskmaster.backend.ai.domain.model.aggregates;

import lombok.Getter;
import pe.edu.upc.taskmaster.backend.ai.domain.model.valueobjects.SmartVelocity;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class MemberWeeklySummary {
    private final Long userId;
    private final String userName;
    private final SmartVelocity smartVelocity;
    private final String summaryText;
    private final String riskPrediction;
    private final List<String> performanceSuggestions;
    private final LocalDateTime generatedAt;

    private MemberWeeklySummary(Builder builder) {
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.smartVelocity = builder.smartVelocity;
        this.summaryText = builder.summaryText;
        this.riskPrediction = builder.riskPrediction;
        this.performanceSuggestions = builder.performanceSuggestions;
        this.generatedAt = builder.generatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private String userName;
        private SmartVelocity smartVelocity;
        private String summaryText;
        private String riskPrediction;
        private List<String> performanceSuggestions;
        private LocalDateTime generatedAt;

        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder userName(String userName) { this.userName = userName; return this; }
        public Builder smartVelocity(SmartVelocity smartVelocity) { this.smartVelocity = smartVelocity; return this; }
        public Builder summaryText(String summaryText) { this.summaryText = summaryText; return this; }
        public Builder riskPrediction(String riskPrediction) { this.riskPrediction = riskPrediction; return this; }
        public Builder performanceSuggestions(List<String> performanceSuggestions) { this.performanceSuggestions = performanceSuggestions; return this; }
        public Builder generatedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; return this; }
        public MemberWeeklySummary build() { return new MemberWeeklySummary(this); }
    }
}
