package com.hodali.classsync.e2e;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;

import static org.junit.jupiter.api.Assertions.*;

public class FullFlowE2ETest extends BaseE2ETest {

    private void logout() {
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.get(BASE_URL + "/login");
        waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT);
    }

    @Test
    void test_complete_attendance_flow() {
        // 1. Login as teacher
        loginAsTeacherWithRetry();

        // 2. Create an attendance session
        waitAndType(By.cssSelector("input[name='courseName']"), "Software Engineering 101");
        waitAndType(By.cssSelector("input[name='validFor']"), "30");
        waitAndClick(By.cssSelector("button[type='submit']"));

        // 3. Capture the 6-digit code
        sleep(500);
        String code = waitForElement(By.cssSelector("p.font-mono"), 30).getText();
        assertNotNull(code, "Code should be generated");
        assertTrue(code.matches("\\d{6}"), "Code should be 6 digits, got: " + code);

        // 4. Logout
        logout();

        // 5. Login as student
        loginAsStudentWithRetry();

        // 6. Enter the 6-digit code
        waitAndType(By.cssSelector("input[name='code']"), code);

        // 7. Submit check-in
        waitAndClick(By.cssSelector("button[type='submit']"));

        // 8. Verify student sees PRESENT status
        String statusText = getElementText(By.cssSelector("p.text-3xl"));
        assertEquals("PRESENT", statusText, "Student should be marked PRESENT");

        // 9. Logout
        logout();

        // 10. Login as teacher again
        loginAsTeacherWithRetry();

        // 11-12. Verify we're back on the teacher dashboard
        String heading = getElementText(By.tagName("h2"));
        assertTrue(heading.contains("Teacher Dashboard"),
                "Teacher should be back on dashboard after full flow");
    }
}
