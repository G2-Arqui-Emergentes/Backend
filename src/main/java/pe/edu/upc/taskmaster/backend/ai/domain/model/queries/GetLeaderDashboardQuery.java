package pe.edu.upc.taskmaster.backend.ai.domain.model.queries;

public record GetLeaderDashboardQuery(Long leaderId) {
    public GetLeaderDashboardQuery {
        if (leaderId == null || leaderId <= 0) {
            throw new IllegalArgumentException("Leader ID cannot be null or empty");
        }
    }
}
