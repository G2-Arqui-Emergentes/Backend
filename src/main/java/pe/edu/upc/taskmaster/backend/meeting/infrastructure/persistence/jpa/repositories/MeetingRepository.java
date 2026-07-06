package pe.edu.upc.taskmaster.backend.meeting.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.edu.upc.taskmaster.backend.meeting.domain.model.aggregates.Meeting;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByLeaderId(Long leaderId);
}
