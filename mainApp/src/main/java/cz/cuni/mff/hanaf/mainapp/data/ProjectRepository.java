package cz.cuni.mff.hanaf.mainapp.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByAccessibleUsers_Id(long id);
    List<Project> findByAdminUsers_Id(long id);
}
