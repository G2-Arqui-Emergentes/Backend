package pe.edu.upc.managewise.backend.task.interfaces.rest.resources;

public record RemoveUserFromTaskResource(
        Long userId
) {
    public RemoveUserFromTaskResource {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
    }
}
