package pe.edu.upc.managewise.backend.project.domain.model.commands;

import java.util.Date;

public record SetCodeCommand(
        Long projectId,
        String keycode,
        Date expiration
) {
}
