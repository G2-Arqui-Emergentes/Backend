package pe.edu.upc.taskmaster.backend.project.domain.model.commands;

import java.util.Date;

public record SetCodeCommand(
        Long projectId,
        String keycode,
        Date expiration
) {
}
