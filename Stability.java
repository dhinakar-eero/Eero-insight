package org.insight;


import org.openqa.selenium.By;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;



public class Stability {
    WebDriver driver;
    // Constructor to initialize driver
    public Stability(WebDriver driver) {
        this.driver = driver;
    }
    public void scrollToDevices() {
        try {
    	        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    	        // Wait for the device section to be present
    	        WebElement deviceSection = wait.until(ExpectedConditions.presenceOfElementLocated(
    	            By.cssSelector("div.eero-insight-device-cards.pb-14[data-testid='regular-devices-dataset']")
    	        ));
    	        // Scroll to the element
    	        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'start', behavior: 'smooth'});", deviceSection);
    	        System.out.println("Scrolled to the device section successfully.");
    	    } catch (Exception e) {
    	        System.out.println("Error while scrolling to the device section: " + e.getMessage());
    	    }
    }
    public void getDeviceCount() throws IOException, GeneralSecurityException, InterruptedException {
    	
    	   WebElement onlineDevicesElement = driver.findElement(By.xpath("//*[@id=\"rc-tabs-1-tab-connectedDevices\"]"));
           String onlineDevicesText = onlineDevicesElement.getText();
           int onlineCount = Integer.parseInt(onlineDevicesText.replaceAll("[^0-9]", ""));
           WebElement offlineDevicesElement = driver.findElement(By.xpath("//*[@id=\"rc-tabs-1-tab-recentlyConnectedDevices\"]"));
           String offlineDevicesText = offlineDevicesElement.getText();
           int offlineCount = Integer.parseInt(offlineDevicesText.replaceAll("[^0-9]", ""));
           WebElement totalDevicesElement = driver.findElement(By.xpath("//*[@id=\"rc-tabs-1-tab-allDevices\"]"));
           String totalCountText = totalDevicesElement.getText();
           int totalCount = Integer.parseInt(totalCountText.replaceAll("[^0-9]", ""));
           
           DailyUsageTracker dailyUsageTracker = new DailyUsageTracker(driver);
           dailyUsageTracker.trackDailyUsage(); // This will log download/upload data in console
           // Extract download and upload data using JavaScriptExecutor
           JavascriptExecutor js = (JavascriptExecutor) driver;
           String downloadData = "";
           String uploadData = "";
           try {
               WebElement downloadElement = driver.findElement(By.xpath("//div[text()='Download']/following-sibling::div[contains(@class, 'text-xl')]"));
               downloadData = (String) js.executeScript("return arguments[0].textContent.trim();", downloadElement);
           } catch (Exception e) {
               System.out.println(":x: Error fetching download data: " + e.getMessage());
           }
           try {
               WebElement uploadElement = driver.findElement(By.xpath("//div[text()='Upload']/following-sibling::div[contains(@class, 'text-xl')]"));
               uploadData = (String) js.executeScript("return arguments[0].textContent.trim();", uploadElement);
           } catch (Exception e) {
               System.out.println(":x: Error fetching upload data: " + e.getMessage());
           }
           // Print results
           System.out.println("Online Devices: " + onlineCount);
           System.out.println("Offline Devices: " + offlineCount);
           System.out.println("Total Devices: " + totalCount);
           System.out.println("Download Data: " + downloadData);
           System.out.println("Upload Data: " + uploadData);           
           GoogleSheetsUpdater.updateGoogleSheet(onlineCount, offlineCount, totalCount, downloadData, uploadData);          
     
    }
}
