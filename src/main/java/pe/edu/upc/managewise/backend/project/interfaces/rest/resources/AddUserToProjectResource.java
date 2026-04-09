package pe.edu.upc.managewise.backend.project.interfaces.rest.resources;

public record AddUserToProjectResource(
        Long memberId,
        String code
) {
    public AddUserToProjectResource {
        if (memberId == null || memberId <= 0) {
            throw new IllegalArgumentException("Member ID cannot be null");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or empty");
        }
    }
}
