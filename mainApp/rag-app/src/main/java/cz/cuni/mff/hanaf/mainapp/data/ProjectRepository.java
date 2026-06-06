package cz.cuni.mff.hanaf.mainapp.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Project} entities.
 * Extends {@link JpaRepository} with additional queries for user-based and public project lookup.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByAccessibleUsers_Id(long id);
    List<Project> findByAdminUsers_Id(long id);
    List<Project> findByIsPublicTrue();

    /**
     * Returns the project with the given id, with accessibleUsers and adminUsers eagerly fetched.
     * Use instead of {@link #findById(Object)} when user collections are needed, to avoid lazy-loading issues.
     *
     * @param id the id of the project to find
     * @return the project, or an empty {@link Optional} if not found
     */
    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.accessibleUsers " +
            "LEFT JOIN FETCH p.adminUsers " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithUsers(@Param("id") Long id);
}
