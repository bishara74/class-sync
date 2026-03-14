package com.hodali.classsync.e2e;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeacherFlowE2ETest extends BaseE2ETest {

    @Test
    @Order(1)
    void test_teacher_dashboard_loads() {
        loginAsTeacherWithRetry();

        String heading = getElementText(By.tagName("h2"));
        assertTrue(heading.contains("Teacher Dashboard"), "Should display Teacher Dashboard heading");

        assertNotNull(waitForElement(By.cssSelector("input[name='courseName']"), DEFAULT_TIMEOUT));
        assertNotNull(waitForElement(By.cssSelector("input[name='validFor']"), DEFAULT_TIMEOUT));
        assertNotNull(waitForElement(By.cssSelector("button[type='submit']"), DEFAULT_TIMEOUT));
    }

    @Test
    @Order(2)
    void test_teacher_creates_session() {
        loginAsTeacherWithRetry();

        waitAndType(By.cssSelector("input[name='courseName']"), "Software Engineering 101");
        waitAndType(By.cssSelector("input[name='validFor']"), "30");
        waitAndClick(By.cssSelector("button[type='submit']"));

        sleep(500);
        String code = waitForElement(By.cssSelector("p.font-mono"), 30).getText();
        assertNotNull(code, "Generated code should appear");
        assertTrue(code.matches("\\d{6}"), "Code should be a 6-digit number, got: " + code);
    }

    @Test
    @Order(3)
    void test_teacher_sees_generated_code_with_correct_format() {
        loginAsTeacherWithRetry();

        waitAndType(By.cssSelector("input[name='courseName']"), "Data Structures");
        waitAndType(By.cssSelector("input[name='validFor']"), "15");
        waitAndClick(By.cssSelector("button[type='submit']"));

        sleep(500);
        String shareText = waitForElement(By.cssSelector("div.bg-green-50 p.text-sm"), 30).getText();
        assertTrue(shareText.contains("Share this code"), "Should display sharing instructions");

        String code = waitForElement(By.cssSelector("div.bg-green-50 p.font-mono"), 30).getText();
        assertEquals(6, code.length(), "Code should be exactly 6 characters");
    }
}
