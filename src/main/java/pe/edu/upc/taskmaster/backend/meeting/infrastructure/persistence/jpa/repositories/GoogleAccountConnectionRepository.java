package pe.edu.upc.taskmaster.backend.meeting.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.GoogleAccountConnection;

import java.util.Optional;

@Repository
public interface GoogleAccountConnectionRepository extends JpaRepository<GoogleAccountConnection, Long> {
    Optional<GoogleAccountConnection> findByUserId(Long userId);
}
