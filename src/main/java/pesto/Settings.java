package pesto;

import java.io.File;

public class Settings {

	public static String fileSep = File.separator;

	/* output folders. */
	public static String OUTPUT_FOLDER = "output" + fileSep;
	public static String SCREENSHOTS_FOLDER = OUTPUT_FOLDER + "screenshots" + fileSep;

	/* visual to DOM locators mapping file. */
	public static String LOCATORS_FILE = SCREENSHOTS_FOLDER + "locators.txt";

	/* DOM-based and visual page objects, and test cases. */
	public static String PO_SELENIUM_FOLDER = "src" + fileSep + "main" + fileSep + "resources" + fileSep + "poSelenium"
			+ fileSep;
	public static String TESTSUITE_FOLDER = "src" + fileSep + "main" + fileSep + "resources" + fileSep + "testSuite"
			+ fileSep;
	public static String PO_SIKULI_FOLDER = "src" + fileSep + "main" + fileSep + "resources" + fileSep + "poSikuli"
			+ fileSep;

	/* DOM-based commands PESTO should managed. */
	public static boolean CAPTURE_CLICKS = true;
	public static boolean CAPTURE_SENDKEYS = true;
	public static boolean CAPTURE_GETTEXT = true;
	public static boolean CAPTURE_SELECT = true;
	public static boolean CAPTURE_CLEAR = true;
}
