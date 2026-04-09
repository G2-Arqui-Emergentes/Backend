package pe.edu.upc.managewise.backend.project.domain.model.commands;

public record RemoveUserFromProjectCommand(
        Long memberId,
        Long projectId
) {
    public RemoveUserFromProjectCommand {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        if (projectId == null || projectId <= 0) {
            throw new IllegalArgumentException("Project ID cannot be null");
        }
    }
}