package com.hodali.classsync.service;

import com.hodali.classsync.model.AttendanceRecord;
import com.hodali.classsync.model.AttendanceSession;
import com.hodali.classsync.model.User;
import com.hodali.classsync.model.enums.AttendanceStatus;
import com.hodali.classsync.repository.AttendanceRecordRepository;
import com.hodali.classsync.repository.AttendanceSessionRepository;
import com.hodali.classsync.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final AttendanceRecordRepository recordRepository; // Added this!

    public AttendanceService(AttendanceSessionRepository sessionRepository,
                             UserRepository userRepository,
                             AttendanceRecordRepository recordRepository) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.recordRepository = recordRepository;
    }

    public AttendanceSession createSession(Long teacherId, String courseName, int validForMinutes) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        String code = String.format("%06d", new Random().nextInt(1000000));

        AttendanceSession session = new AttendanceSession();
        session.setTeacher(teacher);
        session.setCourseName(courseName);
        session.setGeneratedCode(code);
        session.setExpirationTime(LocalDateTime.now().plusMinutes(validForMinutes));

        return sessionRepository.save(session);
    }

    public AttendanceRecord checkIn(Long studentId, String code) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        AttendanceSession session = sessionRepository.findByGeneratedCode(code)
                .orElseThrow(() -> new RuntimeException("Invalid attendance code"));

        if (recordRepository.existsByStudentIdAndSessionId(studentId, session.getId())) {
            throw new RuntimeException("Student has already checked in");
        }

        AttendanceStatus status = LocalDateTime.now().isBefore(session.getExpirationTime())
                ? AttendanceStatus.PRESENT
                : AttendanceStatus.LATE;

        AttendanceRecord record = new AttendanceRecord();
        record.setStudent(student);
        record.setSession(session);
        record.setCheckInTime(LocalDateTime.now());
        record.setStatus(status);

        return recordRepository.save(record);
    }
}