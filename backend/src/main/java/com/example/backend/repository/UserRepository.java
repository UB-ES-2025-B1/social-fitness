package com.example.backend.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  Optional<User> findByUsername(String username);
  Optional<User> findByEmail(String email);
  List<User> findByUsernameContainingIgnoreCase(String username);
  @Query(value = "SELECT u.* FROM users u JOIN event_participants ep ON ep.user_id = u.id WHERE ep.event_id = :eventId", nativeQuery = true)
  List<User> findParticipantsByEventId(@Param("eventId") Long eventId);

}
