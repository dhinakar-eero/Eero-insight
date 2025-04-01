package org.insight;


import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
public class DailyUsageTracker {
    private WebDriver driver;
    private WebDriverWait wait;
    // Constructor
    public DailyUsageTracker(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }
    public void trackDailyUsage() {
        try {
            // Click Analytics Button
            WebElement analyticsButton = driver.findElement(By.xpath(
                "/html/body/div[2]/div/div/div[1]/div[2]/section/div[2]/nav/ul/div[5]/li/a/div/div[2]/button"));
            analyticsButton.click();
           
            Thread.sleep(2000);
            WebElement dataUsageButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(
                    "/html/body/div[2]/div/div/div[1]/div[2]/section/div[2]/nav/ul/div[5]/div/div/ul/li[6]/a")));
            // Scroll manually with an offset
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, -100); arguments[0].scrollIntoView({block: 'center'});", dataUsageButton);
            dataUsageButton.click();

            WebElement dayButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                    "/html/body/div[2]/div/div/div[3]/main/div[3]/div[2]/div[1]/div[2]/div/label[1]/span[2]"))); // Adjust XPath as needed
                dayButton.click();
            Thread.sleep(3000);
            try {
                // Wait for Download Data
                WebElement downloadValueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//div[text()='Download']/following-sibling::div[contains(@class, 'text-xl')]")));
                String downloadData = downloadValueElement.getText().trim();
            } catch (Exception e) {
                System.out.println("Download Data not found.");
            }
            try {
                // Wait for Upload Data
                WebElement uploadValueElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(
                    "//div[text()='Upload']/following-sibling::div[contains(@class, 'text-xl')]")));
                String uploadData = uploadValueElement.getText().trim();
            } catch (Exception e) {
                System.out.println("Upload Data not found.");
            }
        } catch (Exception e) {
            
        }
    }
}
