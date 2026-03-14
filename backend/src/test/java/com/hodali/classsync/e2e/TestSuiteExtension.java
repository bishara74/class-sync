package com.hodali.classsync.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public class TestSuiteExtension implements BeforeAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
            ExtensionContext.Namespace.create(TestSuiteExtension.class);

    private static volatile WebDriver sharedDriver;
    private static ChromeOptions chromeOptions;

    public static synchronized WebDriver getDriver() {
        if (sharedDriver == null) {
            createDriver();
        }
        // Test if session is still alive; recreate if crashed
        try {
            sharedDriver.getTitle();
        } catch (Exception e) {
            System.err.println("Chrome session dead, recreating driver...");
            try { sharedDriver.quit(); } catch (Exception ignored) {}
            createDriver();
        }
        return sharedDriver;
    }

    private static void createDriver() {
        if (chromeOptions == null) {
            WebDriverManager.chromedriver().setup();
            chromeOptions = new ChromeOptions();
            chromeOptions.addArguments("--headless");
            chromeOptions.addArguments("--no-sandbox");
            chromeOptions.addArguments("--disable-dev-shm-usage");
            chromeOptions.addArguments("--disable-gpu");
            chromeOptions.addArguments("--disable-extensions");
            chromeOptions.addArguments("--window-size=1920,1080");
        }
        sharedDriver = new ChromeDriver(chromeOptions);
        sharedDriver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot().getStore(NAMESPACE)
                .getOrComputeIfAbsent("driver", key -> new SharedDriverResource(), SharedDriverResource.class);
    }

    private static class SharedDriverResource implements ExtensionContext.Store.CloseableResource {

        SharedDriverResource() {
            getDriver(); // ensure driver is created
        }

        @Override
        public void close() {
            if (sharedDriver != null) {
                try { sharedDriver.quit(); } catch (Exception ignored) {}
                sharedDriver = null;
            }
        }
    }
}
