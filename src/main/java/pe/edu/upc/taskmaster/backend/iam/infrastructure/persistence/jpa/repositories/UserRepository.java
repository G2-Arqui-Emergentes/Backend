package pe.edu.upc.taskmaster.backend.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.upc.taskmaster.backend.iam.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

/**
 * This interface is responsible for providing the User entity related operations.
 * It extends the JpaRepository interface.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>
{
    /**
     * This method is responsible for finding the user by username.
     * @param email The username.
     * @return The user if found, empty otherwise.
     */
  Optional<User> findByEmail(String email);

  /**
   * This method is responsible for checking if the user exists by username.
   * @param email The username.
   * @return true if the user exists, false otherwise.
   */
  boolean existsByEmail(String email);

  @Query("SELECT u FROM User u JOIN u.memberInProjects p WHERE p.id = :projectId")
  List<User> findByMemberInProjectsId(@Param("projectId") Long projectId);
}
