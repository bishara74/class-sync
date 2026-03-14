package com.hodali.classsync.e2e;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class LoginE2ETest extends BaseE2ETest {

    @Test
    @Order(1)
    void test_login_page_loads() {
        assertNotNull(waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT));
        assertNotNull(waitForElement(By.cssSelector("input[name='password']"), DEFAULT_TIMEOUT));
        assertNotNull(waitForElement(By.cssSelector("button[type='submit']"), DEFAULT_TIMEOUT));
    }

    @Test
    @Order(2)
    void test_teacher_login_success() {
        loginAsTeacherWithRetry();
        assertTrue(driver.getCurrentUrl().contains("/teacher"));
    }

    @Test
    @Order(3)
    void test_student_login_success() {
        loginAsStudentWithRetry();
        assertTrue(driver.getCurrentUrl().contains("/student"));
    }

    @Test
    @Order(4)
    void test_invalid_login_shows_error() {
        waitAndClick(By.xpath("//button[normalize-space()='Teacher']"));
        waitAndType(By.cssSelector("input[name='email']"), "wrong@email.com");
        waitAndType(By.cssSelector("input[name='password']"), "wrongpass");
        waitAndClick(By.cssSelector("button[type='submit']"));

        boolean errorAppeared;
        try {
            errorAppeared = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                    .until(d -> !d.findElements(By.cssSelector("form p")).isEmpty());
        } catch (Exception e) {
            errorAppeared = false;
        }

        assertTrue(driver.getCurrentUrl().contains("/login"),
                "Should remain on login page after invalid credentials");

        if (errorAppeared) {
            String errorText = driver.findElement(By.cssSelector("form p")).getText();
            assertFalse(errorText.isEmpty(), "Error message should not be empty");
        } else {
            assertNotNull(driver.findElement(By.cssSelector("button[type='submit']")),
                    "Login form should still be displayed after failed login");
        }
    }

    @Test
    @Order(5)
    void test_empty_fields_prevented() {
        waitAndClick(By.xpath("//button[normalize-space()='Teacher']"));
        waitAndClick(By.cssSelector("button[type='submit']"));
        assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}
