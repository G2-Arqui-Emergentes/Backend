package pe.edu.upc.managewise.backend.project.interfaces.rest.transform;
import pe.edu.upc.managewise.backend.project.domain.model.commands.UpdateProjectCommand;
import pe.edu.upc.managewise.backend.project.interfaces.rest.resources.UpdateProjectResource;
import pe.edu.upc.managewise.backend.project.domain.model.valueobjects.ProjectStatus;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UpdateProjectCommandFromResourceAssembler {
    public static UpdateProjectCommand toCommandFromResource(UpdateProjectResource resource, Long projectId) {

        ProjectStatus status = ProjectStatus.valueOf(resource.status().toUpperCase());
        Date endDate = convertStringToDate(resource.endDate());
        Double budget = resource.budget() != null ? resource.budget() : null;

        return new UpdateProjectCommand(
                projectId,
                resource.name(),
                resource.description(),
                resource.imageUrl(),
                budget,
                status,
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
