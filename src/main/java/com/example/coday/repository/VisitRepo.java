package com.example.coday.repository;

import com.example.coday.model.User;
import com.example.coday.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitRepo extends JpaRepository<Visit, Long> {

    Optional<Visit> findByUserIdAndCheckInTimeBetween(Long userId, LocalDateTime start, LocalDateTime end);

    boolean existsByUser(User user);

    boolean existsByUserAndCheckOutTimeIsNull(User user);

    Optional<Visit> findFirstByUserAndCheckOutTimeIsNullOrderByCheckInTimeDesc(User user);

    List<Visit> findByUserOrderByCheckInTimeDesc(User user);

    Optional<Visit> findByUserAndCheckOutTimeIsNull(User user);
}
