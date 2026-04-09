package pe.edu.upc.managewise.backend.iam.domain.model.queries;

public record GetUserByIdQuery(Long userId) {
    public GetUserByIdQuery {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("UserId cannot be empty or negative");
        }
    }
}
