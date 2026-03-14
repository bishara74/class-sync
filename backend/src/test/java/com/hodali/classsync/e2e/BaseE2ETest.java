package com.hodali.classsync.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@ExtendWith(TestSuiteExtension.class)
public abstract class BaseE2ETest {

    protected static final String BASE_URL = "http://localhost:4200";
    protected static final String TEACHER_EMAIL = "teacher@school.edu";
    protected static final String TEACHER_PASSWORD = "pass123";
    protected static final String STUDENT_EMAIL = "student@school.edu";
    protected static final String STUDENT_PASSWORD = "pass123";
    protected static final String STUDENT_NEPTUN = "ABC123";

    protected static final int DEFAULT_TIMEOUT = 15;

    protected WebDriver driver;

    @BeforeEach
    void resetState() {
        driver = TestSuiteExtension.getDriver();
        driver.get(BASE_URL + "/login");
        waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT);
        ((JavascriptExecutor) driver).executeScript("window.localStorage.clear();");
        driver.navigate().refresh();
        waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT);
        sleep(500);
    }

    protected WebElement waitForElement(By locator, int timeoutSeconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void waitAndClick(By locator) {
        new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT))
                .until(ExpectedConditions.elementToBeClickable(locator))
                .click();
    }

    protected void waitAndType(By locator, String text) {
        WebElement element = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.elementToBeClickable(locator));
        element.click();
        sleep(100);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"));
        element.sendKeys(text);
        sleep(100);
    }

    protected void waitForUrl(String urlContains, int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.urlContains(urlContains));
    }

    protected String getElementText(By locator) {
        return waitForElement(locator, DEFAULT_TIMEOUT).getText();
    }

    protected void loginAsTeacherWithRetry() {
        performApiLogin(TEACHER_EMAIL, TEACHER_PASSWORD, null);
        driver.get(BASE_URL + "/teacher");
        waitForUrl("/teacher", 15);
        sleep(500);
    }

    protected void loginAsStudentWithRetry() {
        performApiLogin(STUDENT_EMAIL, STUDENT_PASSWORD, STUDENT_NEPTUN);
        driver.get(BASE_URL + "/student");
        waitForUrl("/student", 15);
        sleep(500);
    }

    private void performApiLogin(String email, String password, String neptunCode) {
        // Navigate to any page in the app domain so localStorage is accessible
        if (driver.getCurrentUrl().startsWith("data:") || !driver.getCurrentUrl().startsWith(BASE_URL)) {
            driver.get(BASE_URL + "/login");
            waitForElement(By.cssSelector("input[name='email']"), DEFAULT_TIMEOUT);
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;
        String bodyJson = neptunCode != null
                ? String.format("{\"email\":\"%s\",\"password\":\"%s\",\"neptunCode\":\"%s\"}", email, password, neptunCode)
                : String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

        Object result = js.executeAsyncScript(
                "var callback = arguments[arguments.length - 1];" +
                "(async function() {" +
                "  try {" +
                "    var response = await fetch('http://localhost:8081/api/auth/login', {" +
                "      method: 'POST'," +
                "      headers: { 'Content-Type': 'application/json' }," +
                "      body: '" + bodyJson + "'" +
                "    });" +
                "    var data = await response.json();" +
                "    if (!response.ok) { callback('FAIL:' + response.status + ':' + JSON.stringify(data)); return; }" +
                "    localStorage.setItem('jwt_token', data.token);" +
                "    localStorage.setItem('user', JSON.stringify(data.user));" +
                "    callback('OK');" +
                "  } catch(e) { callback('ERROR:' + e.message); }" +
                "})();"
        );

        String resultStr = result != null ? result.toString() : "null";
        if (!resultStr.equals("OK")) {
            throw new RuntimeException("API login failed: " + resultStr);
        }
    }

    protected void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
