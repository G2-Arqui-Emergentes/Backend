package pe.edu.upc.taskmaster.backend.project.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.taskmaster.backend.project.domain.model.aggregates.Project;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByLeaderId(Long leaderId);
}
