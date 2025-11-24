package cz.cuni.mff.hanaf.mainapp.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByAccessibleUsers_Id(long id);
    List<Project> findByAdminUsers_Id(long id);
    @Query("SELECT p FROM Project p " +
            "LEFT JOIN FETCH p.accessibleUsers " +
            "LEFT JOIN FETCH p.adminUsers " +
            "WHERE p.id = :id")
    Optional<Project> findByIdWithUsers(@Param("id") Long id);
}
