package cz.cuni.mff.hanaf.mainapp.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByProject_Id(long id);
    @Transactional
    void deleteByProjectId(Long projectId);
}
