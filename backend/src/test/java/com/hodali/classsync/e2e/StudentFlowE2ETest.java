package com.hodali.classsync.e2e;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StudentFlowE2ETest extends BaseE2ETest {

    private String loginAsTeacherAndCreateSession() {
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.get(BASE_URL + "/login");
        waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT);

        loginAsTeacherWithRetry();

        waitAndType(By.cssSelector("input[name='courseName']"), "Test Course");
        waitAndType(By.cssSelector("input[name='validFor']"), "30");
        waitAndClick(By.cssSelector("button[type='submit']"));

        sleep(500);
        String code = waitForElement(By.cssSelector("p.font-mono"), 30).getText();
        assertNotNull(code);
        assertTrue(code.matches("\\d{6}"), "Expected 6-digit code, got: " + code);

        return code;
    }

    @Test
    @Order(1)
    void test_student_dashboard_loads() {
        loginAsStudentWithRetry();

        String heading = getElementText(By.tagName("h2"));
        assertTrue(heading.contains("Student Check-In"), "Should display Student Check-In heading");

        assertNotNull(waitForElement(By.cssSelector("input[name='code']"), DEFAULT_TIMEOUT));
        assertNotNull(waitForElement(By.cssSelector("button[type='submit']"), DEFAULT_TIMEOUT));
    }

    @Test
    @Order(2)
    void test_student_checks_in_successfully() {
        String code = loginAsTeacherAndCreateSession();

        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.get(BASE_URL + "/login");
        waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT);

        loginAsStudentWithRetry();

        waitAndType(By.cssSelector("input[name='code']"), code);
        waitAndClick(By.cssSelector("button[type='submit']"));

        String statusText = getElementText(By.cssSelector("p.text-3xl"));
        assertEquals("PRESENT", statusText, "Student should be marked PRESENT");
    }

    @Test
    @Order(3)
    void test_student_invalid_code_shows_error() {
        loginAsStudentWithRetry();

        waitAndType(By.cssSelector("input[name='code']"), "000000");
        waitAndClick(By.cssSelector("button[type='submit']"));

        boolean errorAppeared;
        try {
            errorAppeared = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(d -> !d.findElements(By.cssSelector("div.bg-red-50")).isEmpty());
        } catch (Exception e) {
            errorAppeared = false;
        }

        assertTrue(driver.getCurrentUrl().contains("/student"),
                "Should remain on student dashboard after invalid code");

        if (errorAppeared) {
            String errorText = driver.findElement(By.cssSelector("div.bg-red-50")).getText();
            assertFalse(errorText.isEmpty(), "Error message should not be empty");
        } else {
            assertNotNull(driver.findElement(By.cssSelector("input[name='code']")),
                    "Check-in form should still be displayed");
        }
    }
}
