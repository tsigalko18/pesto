package poSelenium;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import testSuite.BaseTest;

public class IndexPage {

	private WebDriver driver;

	@FindBy(xpath = "html/body/div[1]/div[2]/div/ul/li[1]/a")
	private WebElement home;

	@FindBy(xpath = "html/body/div[1]/div[3]/div/div[1]/div/h2")
	private WebElement title;

	public IndexPage() {
		this.driver = BaseTest.getDriver();
		PageFactory.initElements(driver, this);
	}

	public void goToHome() {
		home.click();
	}

	public boolean getPageTitle(String s) throws InterruptedException {
		return title.getText().contains(s);
	}

}
