package pe.edu.upc.managewise.backend.project.domain.model.commands;

public record AddUserToProjectCommand(
        Long memberId,
        String code
) {
    public AddUserToProjectCommand {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
    }
}
