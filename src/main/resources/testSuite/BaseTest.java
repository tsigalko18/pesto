package testSuite;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class BaseTest {

	protected static WebDriver driver;

	public static WebDriver getDriver() {
		if (driver == null)
			driver = new FirefoxDriver();
		return driver;
	}

	@Before
	public void setUp() throws InterruptedException {
		driver = getDriver();
		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
		driver.get("http://sepl.dibris.unige.it/index.php");
		driver.manage().window().maximize();
	}

	@After
	public void tearDown() throws Exception {
		driver.quit();
		driver = null;
	}

}
