package org.insight;


import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.ProfilesIni;
import java.io.IOException;
import java.security.GeneralSecurityException;
import io.github.bonigarcia.wdm.WebDriverManager;




public class Eero_insight {
	
	public static void main(String [] args) throws InterruptedException, IOException, GeneralSecurityException{
	      WebDriverManager.firefoxdriver().setup();
	        
	        ProfilesIni profile = new ProfilesIni();
	        FirefoxProfile myProfile = profile.getProfile("selenium-profile"); // Match profile name
	        FirefoxOptions options = new FirefoxOptions();
	        options.setProfile(myProfile);
	        
	        WebDriver driver = new FirefoxDriver(options);
	        
	        driver.get("https://insight.stage.e2ro.com/networks/906402");
	        Thread.sleep(3000);
	        
	        
	        Stability stability = new Stability(driver);
	        DailyUsageTracker datatracker = new DailyUsageTracker(driver);
	        
	        
	        stability.scrollToDevices();
	        stability.getDeviceCount();
	        
	        datatracker.trackDailyUsage();
	       
	        driver.close();;
	        
	}

}

