package com.example.coday.repository;

import com.example.coday.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findBySsn(String ssn);
    List<User> findByAdminCommentIsNotNull();
    List<User> findByCompanyId(Long companyId);
    List<User> findByEmailContainingIgnoreCase(String email);
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    List<User> findByPointsGreaterThanEqual(int points);
    List<User> findByPointsLessThanEqual(int points);
    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND u.points >= :minPoints")
    List<User> searchByNameAndMinPoints(@Param("name") String name, @Param("minPoints") int minPoints);

    List<User> findByApprovedTrue();

    List<User> findAllByRole(User.Role role);

    Optional<User> findByEmailIgnoreCase(String email);
    List<User> findByCompanyIdAndApprovedTrue(Long companyId);

}
