package com.hodali.classsync.controller;

import com.hodali.classsync.dto.CheckInRequest;
import com.hodali.classsync.dto.CreateSessionRequest;
import com.hodali.classsync.model.AttendanceRecord;
import com.hodali.classsync.model.AttendanceSession;
import com.hodali.classsync.service.AttendanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController // Tells Spring this class handles HTTP requests and returns JSON
@RequestMapping("/api/attendance") // The base URL for all endpoints in this file
@CrossOrigin(origins = "*") // Allows our local React app to talk to this local Backend
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Injecting our service layer
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // Endpoint 1: Teacher generates a code
    // URL: POST http://localhost:8081/api/attendance/sessions
    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSession> createSession(@RequestBody CreateSessionRequest request) {
        AttendanceSession session = attendanceService.createSession(
                request.teacherId(),
                request.courseName(),
                request.validForMinutes()
        );
        return ResponseEntity.ok(session); // Returns a 200 OK status with the JSON session data
    }

    // Endpoint 2: Student enters the code
    // URL: POST http://localhost:8081/api/attendance/check-in
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceRecord> checkIn(@RequestBody CheckInRequest request) {
        AttendanceRecord record = attendanceService.checkIn(
                request.studentId(),
                request.code()
        );
        return ResponseEntity.ok(record);
    }
}