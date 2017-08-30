package pesto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import utils.UtilsOpenCv;

@Aspect
public class VisualLocatorGenerator {

	protected static File locFile;

	static WebDriver d = null;
	static Map<String, LinkedList<String>> list = new HashMap<String, LinkedList<String>>();

	/**
	 * This advice starts at any Selenium WebDriver manage invocation. It sets up
	 * the directories in which PESTO saves the screenshots and all the files.
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.WebDriver.manage())")
	public void logBeforeWebDriverManage(JoinPoint joinPoint) {

		locFile = new File(Settings.LOCATORS_FILE);

		File theDir = new File(Settings.SCREENSHOTS_FOLDER);
		if (!theDir.exists()) {
			System.out.println("creating directory " + Settings.SCREENSHOTS_FOLDER);
			try {
				FileUtils.forceMkdir(theDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (!locFile.exists()) {
			System.out.println("creating file locators.txt");
			try {
				locFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		File poSikuliDir = new File(Settings.PO_SIKULI_FOLDER);

		if (!poSikuliDir.exists()) {
			System.out.println("creating directory " + Settings.PO_SIKULI_FOLDER + "...");
			try {
				FileUtils.forceMkdir(poSikuliDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		WebDriver driver = (WebDriver) joinPoint.getTarget();
		d = driver;
	}

	/**
	 * This advice captures all click calls on any WebElement.
	 * <p>
	 * If the method conversion is activated, the Visual Locator Generator is
	 * triggered.
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.WebElement.click())")
	public void clickInterceptor(JoinPoint joinPoint) {

		if (Settings.CAPTURE_CLICKS) {
			System.out.println("[LOG]\tselenium.WebElement.click() interception is enabled");
			visualLocatorCreator(joinPoint);
		} else {
			System.out.println("[LOG]\tselenium.WebElement.click() interception is disabled");
		}

	}

	/**
	 * This advice captures all sendKeys calls on any WebElement.
	 * <p>
	 * If the method conversion is activated, the Visual Locator Generator is
	 * triggered.
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.WebElement.sendKeys(..))")
	public void sendKeysInterceptor(JoinPoint joinPoint) {

		if (Settings.CAPTURE_SENDKEYS) {
			System.out.println("[LOG]\tselenium.WebElement.sendKeys() interception is enabled");
			visualLocatorCreator(joinPoint);
		} else {
			System.out.println("[LOG]\tselenium.WebElement.sendKeys() interception is disabled");
		}

	}

	/**
	 * This advice captures all getText calls on any WebElement.
	 * <p>
	 * If the method conversion is activated, the Visual Locator Generator is
	 * triggered.
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.WebElement.getText())")
	public void getTextInterceptor(JoinPoint joinPoint) {

		if (Settings.CAPTURE_GETTEXT) {
			System.out.println("[LOG]\tselenium.WebElement.getText() interception is enabled");
			visualLocatorCreator(joinPoint);
		} else {
			System.out.println("[LOG]\tselenium.WebElement.getText() interception is disabled");
		}

	}

	/**
	 * This advice captures all calls on select elements.
	 * <p>
	 * If the method conversion is activated, the Visual Locator Generator is
	 * triggered.
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.support.ui.Select.selectByVisibleText(..))")
	public void selectInterceptor(JoinPoint joinPoint) {

		if (Settings.CAPTURE_SELECT) {
			System.out.println("[LOG]\tselenium.support.ui.Select.selectByVisibleText interception is enabled");
			visualLocatorCreator(joinPoint);
		} else {
			System.out.println("[LOG]\tselenium.support.ui.Select.selectByVisibleText interception is disabled");
		}

	}

	/**
	 * This advice captures all clear calls on any WebElement.
	 * <p>
	 * If the method conversion is activated, the Visual Locator Generator is
	 * triggered.
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.WebElement.clear(..))")
	public void clearInterceptor(JoinPoint joinPoint) {

		if (Settings.CAPTURE_CLEAR) {
			System.out.println("[LOG]\tselenium.WebElement.clear interception is enabled");
			visualLocatorCreator(joinPoint);
		} else {
			System.out.println("[LOG]\tselenium.WebElement.clear interception is disabled");
		}

	}

	/**
	 * Proxy to the visual locator generator advice
	 * 
	 * @param joinPoint
	 */
	public void visualLocatorCreator(JoinPoint joinPoint) {
		VisualLocatorGenerator.logBeforeWebElement(joinPoint);
	}

	/**
	 * The heart of the Visual Locator Generator
	 * 
	 * @param joinPoint
	 */
	private static void logBeforeWebElement(JoinPoint joinPoint) {

		WebElement e = null;
		Select s = null;
		String var = "";

		if (joinPoint.getTarget() instanceof Select) {
			s = (Select) joinPoint.getTarget();
			e = (WebElement) s.getOptions().get(0);
			var = extractLocator(s.getOptions().get(0).toString());
		} else {
			e = (WebElement) joinPoint.getTarget();
			var = extractLocator(joinPoint.getTarget().toString());
		}

		String webElementImageName = joinPoint.getSourceLocation().getFileName().replace(".java", "") + "/"
				+ joinPoint.getSourceLocation().getLine();

		// find the PO name and create the new PO file, if it doesn't exist
		String po = joinPoint.getSourceLocation().getFileName().replace(".java", "");
		String poM = Settings.PO_SIKULI_FOLDER + po.concat(".java");
		File modifiedPo = new File(poM);

		if (!modifiedPo.exists()) {
			try {
				modifiedPo.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		String loc = po + "-" + var;

		// multistate
		if (list.containsKey(loc)) {
			Iterator<String> itr = list.get(loc).iterator();
			while (itr.hasNext()) {
				String el = itr.next();
				webElementImageName = webElementImageName.replace('/', '-');
				if (!el.toLowerCase().contains(webElementImageName.toLowerCase())) {
					System.err.println("[WARNING] MULTISTATE ELEMENT: " + loc);
					webElementImageName = webElementImageName.replace('-', '/');
				}
				webElementImageName = webElementImageName.replace('-', '/');
			}
		}

		List<String> images = new LinkedList<String>();
		int scale = 4;
		boolean matchUnivocal = false;
		try {
			do {
				images = UtilsOpenCv.shoot(d, e, webElementImageName, scale);
				matchUnivocal = UtilsOpenCv.isUniqueMatch(images.get(0), images.get(1), locFile.getPath(),
						Imgproc.TM_CCOEFF_NORMED);
				scale--;
			} while (matchUnivocal == false);

		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}

		if (list.get(loc) == null) {
			LinkedList<String> l = new LinkedList<String>();
			l.add(images.get(1));
			list.put(loc, l);
		} else {
			if (!list.get(loc).contains(images.get(1))) {
				list.get(loc).add(images.get(1));
			}
			list.put(loc, list.get(loc));
		}

		System.out.println(po + " " + var + " " + images.get(1));
	}

	/**
	 * Auxiliary method to extract the locator from the @FindBy Selenium WebDriver
	 * annotation.
	 * 
	 * @param s
	 *            the string representing the annotation
	 * @return
	 */
	private static String extractLocator(String s) {

		String var = s;
		var = (String) var.subSequence(var.indexOf(" -> ") + 4, var.length() - 1);

		if (var.contains("->")) {
			var = (String) var.subSequence(0, var.indexOf(" -> "));
			var = (String) var.substring(0, var.length() - 2);
		}

		var = var.replace(": ", "=\"");
		var = var + "\"";

		// uniform locators notations to Selenium WebDriver's
		if (var.contains("css selector")) {
			var = var.replace("css selector", "css");
		} else if (var.contains("link text")) {
			var = var.replace("link text", "linkText");
		} else if (var.contains("xpath")) {
			;
		} else if (var.contains("name")) {
			;
		}
		return var;
	}

	/**
	 * This advice starts at any Selenium WebDriver quit invocation. It saves all
	 * the locators mapping on the filesystem on a dedicated file
	 * 
	 * @param joinPoint
	 */
	@Before("call(* org.openqa.selenium.WebDriver.quit())")
	public void logBeforeWebDriverQuit(JoinPoint joinPoint) {

		// at the end of the test case, save the all the locators in the file
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(Settings.LOCATORS_FILE, true), "utf-8"));
			BufferedReader br = new BufferedReader(new FileReader(Settings.LOCATORS_FILE));

			for (Entry<String, LinkedList<String>> entry : list.entrySet()) {
				String key = entry.getKey();
				LinkedList<String> value = entry.getValue();
				String fileLocators = FileUtils.readFileToString(locFile);

				for (int i = 0; i < value.size(); i++) {
					if (!fileLocators.contains(key + " -> " + value.get(i))) {
						writer.append(key + " -> " + value.get(i) + "\n");
					}
				}
			}
			br.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
