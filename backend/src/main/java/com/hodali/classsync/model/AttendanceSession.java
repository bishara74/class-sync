package com.hodali.classsync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher; // The teacher who created the session

    private String courseName;

    @Column(nullable = false, unique = true)
    private String generatedCode; // The 6-digit code

    private LocalDateTime expirationTime;

    public AttendanceSession(LocalDateTime expirationTime, Long id, User teacher, String courseName, String generatedCode) {
        this.expirationTime = expirationTime;
        this.id = id;
        this.teacher = teacher;
        this.courseName = courseName;
        this.generatedCode = generatedCode;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getGeneratedCode() {
        return generatedCode;
    }

    public void setGeneratedCode(String generatedCode) {
        this.generatedCode = generatedCode;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public AttendanceSession() {
    }
}