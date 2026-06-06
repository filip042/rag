package cz.cuni.mff.hanaf.mainapp.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for {@link User} entities.
 * Extends {@link JpaRepository} with lookup by username for authentication.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Returns the user with the given username, if one exists.
     *
     * @param username the username to search for
     * @return the user, or an empty {@link Optional} if not found
     */
    Optional<User> findByUsername(String username);
}
