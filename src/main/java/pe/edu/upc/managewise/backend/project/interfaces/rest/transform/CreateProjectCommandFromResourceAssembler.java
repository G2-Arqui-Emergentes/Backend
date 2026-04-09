package pe.edu.upc.managewise.backend.project.interfaces.rest.transform;

import pe.edu.upc.managewise.backend.project.domain.model.commands.CreateProjectCommand;
import pe.edu.upc.managewise.backend.project.interfaces.rest.resources.CreateProjectResource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CreateProjectCommandFromResourceAssembler {
    public static CreateProjectCommand toCommandFromResource(CreateProjectResource resource, Long leaderId) {
        Date endDate = convertStringToDate(resource.endDate());
        return new CreateProjectCommand(
                leaderId,
                resource.name(),
                resource.description(),
                resource.imageUrl(),
                resource.budget(),
                endDate
        );
    }

    private static Date convertStringToDate(String dateString) {
        try {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.parse(dateString);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString, e);
        }
    }
}

