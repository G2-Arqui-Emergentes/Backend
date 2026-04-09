package pe.edu.upc.managewise.backend.project.domain.model.commands;

public record DeleteProjectCommand(Long projectId) {
    public DeleteProjectCommand{
        if(projectId == null|| projectId <= 0){
            throw new IllegalArgumentException("Project ID cannot be null");
        }
    }
}
