package poSikuli;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import testSuite.BaseTest;
import java.net.URL;
import java.io.File;
import java.net.MalformedURLException;
import org.sikuli.api.*;
import org.sikuli.api.robot.*;
import org.sikuli.api.robot.Keyboard;
import org.sikuli.api.visual.*;
import org.sikuli.api.robot.desktop.*;

public class IndexPage {

    private WebDriver driver;

    private Target home;

    private Target title;

    private ScreenRegion screen;

    private ScreenRegion ris;

    private Keyboard keyboard;

    private Mouse mouse;

    protected String urlBase = "";

    public IndexPage() throws MalformedURLException {
        this.driver = BaseTest.getDriver();
        PageFactory.initElements(driver, this);
        screen = new DesktopScreenRegion();
        ris = new DesktopScreenRegion();
        screen = new DesktopScreenRegion();
        mouse = new DesktopMouse();
        keyboard = new DesktopKeyboard();
        home = new ImageTarget(new File(urlBase+"output/screenshots/IndexPage/IndexPage-26.png"));
        title = new ImageTarget(new File(urlBase+"output/screenshots/IndexPage/IndexPage-30.png"));
    }

    public void goToHome() throws MalformedURLException, InterruptedException {
        click(home);
    }

    public boolean getPageTitle(String s) throws MalformedURLException, InterruptedException {
        screen.setScore(1.00);
        ris = screen.find(title);
        Canvas canvas = new DesktopCanvas();
        canvas.addBox(ris).display(1);
        canvas.addLabel(ris, "Evaluating Assertion!").display(1);
        if (ris==null) return false;
		else return true;
    }

    public void click(Target element) throws InterruptedException {
        Thread.sleep(500);
		screen.setScore(1.00);

		if(screen.find(element) == null) { 
			mouse.click(screen.getCenter());
			keyboard.type(Key.PAGE_DOWN);
			Thread.sleep(2000);
 		}

		ris = screen.find(element);
		while(ris == null){
			mouse.wheel(1, 2);
			Thread.sleep(500);
			ris = screen.find(element);
 		}

		Canvas canvas = new DesktopCanvas();
		canvas.addBox(ris).display(1);
		mouse.click(ris.getCenter());
		Thread.sleep(1000);
    }

    public void type(Target element, String value) throws InterruptedException {
        click(element);
        keyboard.type(value);
    }

    public void clear(Target element) throws InterruptedException {
        click(element);
        int i = 100;
        while(i > 0) {;
        	keyboard.type(Key.BACKSPACE);
        	i--;
        };
        //keyboard.type(Key.CMD + Key.ALT + Key.BACKSPACE);
    }

    public void typeSelect(Target element, String value) throws InterruptedException {
        int v = 0;

		try { 
			v = Integer.parseInt(value); 
		} catch (NumberFormatException e) {
			ScreenRegion ris = screen.find(element);
			mouse.click(ris.getCenter());
			keyboard.type(value);
			mouse.click(ris.getCenter());
			return;
		}

		ScreenRegion ris = screen.find(element);
		mouse.click(ris.getCenter());
		for(int i=0; i<=v; i++) {
			keyboard.type(Key.DOWN);
		}
		keyboard.type(Key.ENTER);
		return;
    }
}
