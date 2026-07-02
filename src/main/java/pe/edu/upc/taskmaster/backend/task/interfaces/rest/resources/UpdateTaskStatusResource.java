package pe.edu.upc.taskmaster.backend.task.interfaces.rest.resources;

public record UpdateTaskStatusResource(
        String status
) {
    public UpdateTaskStatusResource {
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
    }
}
