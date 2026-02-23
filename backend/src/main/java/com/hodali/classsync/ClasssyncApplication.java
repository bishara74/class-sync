package com.hodali.classsync;

import com.hodali.classsync.model.User;
import com.hodali.classsync.model.enums.Role;
import com.hodali.classsync.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ClasssyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClasssyncApplication.class, args);
    }

    @Bean
    public CommandLineRunner seedDatabase(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                // Create Teacher
                User teacher = new User("Jane Doe", "teacher@school.edu", Role.TEACHER);
                teacher.setPassword("pass123");
                userRepository.save(teacher);

                // Create Student
                User student = new User("John Smith", "student@school.edu", Role.STUDENT);
                student.setPassword("pass123");
                student.setNeptunCode("ABC123"); // Added Neptun Code!
                userRepository.save(student);

                System.out.println("✅ Test users (with passwords) seeded successfully!");
            }
        };
    }}