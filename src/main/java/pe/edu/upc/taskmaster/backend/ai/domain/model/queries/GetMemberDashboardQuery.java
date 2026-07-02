package pe.edu.upc.taskmaster.backend.ai.domain.model.queries;

public record GetMemberDashboardQuery(Long memberId) {
    public GetMemberDashboardQuery {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("Member ID cannot be null or empty");
        }
    }
}
