package pe.edu.upc.managewise.backend.task.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.managewise.backend.task.domain.model.aggregates.Task;

import java.util.List;
import java.util.Date;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByEndDateBetween(Date start, Date end);
}
