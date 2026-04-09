package pe.edu.upc.managewise.backend.project.domain.model.queries;

public record GetProjectsByMemberIdQuery(Long memberId) {
    public GetProjectsByMemberIdQuery {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
    }
}
