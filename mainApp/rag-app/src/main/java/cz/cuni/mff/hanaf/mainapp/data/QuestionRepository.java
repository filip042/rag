package cz.cuni.mff.hanaf.mainapp.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for {@link Question} entities.
 * Extends {@link JpaRepository} with lookup and deletion by project.
 */
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByProject_Id(long id);

    /**
     * Deletes all questions belonging to the given project.
     * Requires an active transaction, as bulk delete operations are not automatically wrapped by Spring Data.
     *
     * @param projectId the id of the project whose questions should be deleted
     */
    @Transactional
    void deleteByProjectId(Long projectId);
}
