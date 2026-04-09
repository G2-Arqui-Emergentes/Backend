package pe.edu.upc.managewise.backend.task.interfaces.rest.resources;

public record AssignUserToTaskResource(
        Long userId
) {
    public AssignUserToTaskResource {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID cannot be null or negative");
        }
    }
}
