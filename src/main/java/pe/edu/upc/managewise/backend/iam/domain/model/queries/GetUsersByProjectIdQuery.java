package pe.edu.upc.managewise.backend.iam.domain.model.queries;

public record GetUsersByProjectIdQuery(Long projectId) {
    public GetUsersByProjectIdQuery {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("ProjectId cannot be empty or negative");
        }
    }
}
