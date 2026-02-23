package com.hodali.classsync.service;

import com.hodali.classsync.model.AttendanceRecord;
import com.hodali.classsync.model.AttendanceSession;
import com.hodali.classsync.model.User;
import com.hodali.classsync.model.enums.AttendanceStatus;
import com.hodali.classsync.model.enums.Role;
import com.hodali.classsync.repository.AttendanceRecordRepository;
import com.hodali.classsync.repository.AttendanceSessionRepository;
import com.hodali.classsync.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock // Added this!
    private AttendanceRecordRepository recordRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void createSession_ShouldGenerateCodeAndSave() {
        User fakeTeacher = new User("Jane Doe", "jane.doe@gmail.com", Role.TEACHER);
        fakeTeacher.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(fakeTeacher));
        when(sessionRepository.save(any(AttendanceSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceSession result = attendanceService.createSession(1L, "Data Structures", 10);

        assertNotNull(result);
        assertEquals(6, result.getGeneratedCode().length());
        verify(sessionRepository, times(1)).save(any(AttendanceSession.class));
    }

    @Test
    void checkIn_BeforeExpiration_ShouldMarkAsPresent() {
        User student = new User("John Smith", "john@gmail.com", Role.STUDENT);
        student.setId(2L);

        AttendanceSession session = new AttendanceSession();
        session.setId(10L);
        session.setGeneratedCode("123456");
        session.setExpirationTime(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(2L)).thenReturn(Optional.of(student));
        when(sessionRepository.findByGeneratedCode("123456")).thenReturn(Optional.of(session));
        when(recordRepository.existsByStudentIdAndSessionId(2L, 10L)).thenReturn(false);
        when(recordRepository.save(any(AttendanceRecord.class))).thenAnswer(i -> i.getArgument(0));

        AttendanceRecord result = attendanceService.checkIn(2L, "123456");

        assertNotNull(result);
        assertEquals(AttendanceStatus.PRESENT, result.getStatus(), "Student should be marked PRESENT");
        verify(recordRepository, times(1)).save(any(AttendanceRecord.class));
    }
}