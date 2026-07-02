package pe.edu.upc.taskmaster.backend.project.domain.model.queries;

public record GetProjectsByLeaderIdQuery(Long leaderId) {
    public GetProjectsByLeaderIdQuery {
        if (leaderId == null || leaderId <= 0) {
            throw new IllegalArgumentException("Leader ID cannot be null");
        }
    }
}
