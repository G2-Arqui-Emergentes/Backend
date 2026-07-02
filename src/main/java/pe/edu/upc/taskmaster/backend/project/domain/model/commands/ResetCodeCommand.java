package pe.edu.upc.taskmaster.backend.project.domain.model.commands;

public record ResetCodeCommand(
        Long projectId
) {
    public ResetCodeCommand {
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
    }
}
