package com.hodali.classsync.repository;

import com.hodali.classsync.model.AttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    Optional<AttendanceSession> findByGeneratedCode(String generatedCode);
}