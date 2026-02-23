package com.hodali.classsync.repository;

import com.hodali.classsync.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);
}