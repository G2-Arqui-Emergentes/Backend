package pe.edu.upc.taskmaster.backend.task.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.taskmaster.backend.task.domain.model.aggregates.Task;

import java.util.List;
import java.util.Date;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByEndDateBetween(Date start, Date end);

    // NUEVOS MÉTODOS PARA IA
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId")
    List<Task> findTasksByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT t FROM Task t LEFT JOIN t.assignedUsers u WHERE u.id = :userId OR u.id IS NULL")
    List<Task> findByAssignedUserId(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status")
    List<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") String status);
}
