package utils;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;

import pesto.Settings;

public class UtilsOpenCv {

	protected static WebDriver driver;

	static {
		nu.pattern.OpenCV.loadShared();
	}

	/**
	 * This method crops a scaled image of the web element
	 */
	public static List<String> shoot(WebDriver dr, WebElement element, String filename, int scale)
			throws IOException, InterruptedException {

		driver = new Augmenter().augment(dr);

		File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

		highlightElement(element);

		org.openqa.selenium.Point elementCoordinates = element.getLocation();
		int width = element.getSize().getWidth();
		int height = element.getSize().getHeight();
		Rectangle rect = new Rectangle(width, height);

		String[] po = filename.split("/");

		BufferedImage img = ImageIO.read(screen);
		String poDirectory = Settings.SCREENSHOTS_FOLDER + po[0] + "/";

		String filenameScreen = poDirectory + filename.replace("/", "-") + "All.png";
		File screenFile = new File(filenameScreen);
		if (!screenFile.exists())
			ImageIO.write(img, "png", screenFile);

		String relPath = poDirectory + filename.replace("/", "-") + ".png";
		File locFile = new File(relPath);

		if (locFile.exists()) {
			// gestione multipage
			// System.err.println("*** MULTISTATE ELEMENT *** " + filename);
			// locFile = new File(relPath.replace(".png", "state2.png"));
			// ***********************************************///////////////
			// return relPath;
		}

		// ritaglio preciso
		// BufferedImage subImage =
		// img.getSubimage(elementCoordinates.x/*-(rect.width/2)*/,
		// elementCoordinates.y/*-(rect.height/2)*/, /*2**/rect.width,
		// /*2**/rect.height);

		// ritaglio con offset
		// int offset = 0;
		// BufferedImage subImage = img.getSubimage(elementCoordinates.x-offset,
		// elementCoordinates.y-offset, 2*offset+rect.width,
		// 2*offset+rect.height);

		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// int screen_width = (int) (screenSize.getWidth() / 0.33);
		// int screen_height = (int) (screenSize.getHeight() / 0.33);

		// int max_offset_x = Math.min(elementCoordinates.x,
		// screen_width-rect.width-elementCoordinates.x);
		// int max_offset_y = Math.min(elementCoordinates.y,
		// screen_height-rect.height-elementCoordinates.y);
		// int offset = Math.min(max_offset_x, max_offset_y);

		int max_offset_x = Math.min(elementCoordinates.x, img.getWidth() - rect.width - elementCoordinates.x);
		int max_offset_y = Math.min(elementCoordinates.y, img.getHeight() - rect.height - elementCoordinates.y);
		int offset = Math.min(max_offset_x, max_offset_y);
		offset = offset / scale;

		BufferedImage subImage = null;

		try {
			if (element.getTagName().equals("option")) {
				WebElement thisShouldBeTheSelect = element.findElement(By.xpath(".."));
				new Actions(driver).moveToElement(thisShouldBeTheSelect).perform();

				System.err.println("\n\nthisShouldBeTheSelect.getLocation(): " + thisShouldBeTheSelect.getLocation());
				System.err.println("element.getLocation(): " + element.getLocation());

				elementCoordinates = thisShouldBeTheSelect.getLocation();
				subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset,
						2 * offset + rect.width, 2 * offset + rect.height);
			} else {
				subImage = img.getSubimage(elementCoordinates.x - offset, elementCoordinates.y - offset,
						2 * offset + rect.width, 2 * offset + rect.height);
			}
		} catch (RasterFormatException e) {
			System.err.println("[WARNING]\t" + e.getMessage());
		}

		ImageIO.write(subImage, "png", locFile);

		subImage.flush();

		ImageIO.write(subImage, "png", screen);
		FileUtils.copyFile(screen, locFile);

		List<String> ret = new LinkedList<String>();
		ret.add(0, filenameScreen);
		ret.add(1, relPath);

		return ret;
	}

	/**
	 * This method highlights the web element on which PESTO is currently performing
	 * 
	 * @param element
	 * @throws InterruptedException
	 */
	private static void highlightElement(WebElement element) throws InterruptedException {

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element,
				"color: yellow; border: 2px solid yellow;");
		Thread.sleep(200);
		js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "");

	}

	/**
	 * Checks whether the found web element match is unique over the web page
	 * 
	 * @param inFile
	 * @param templateFile
	 * @param outFile
	 * @param match_method
	 * @return
	 */
	public static boolean isUniqueMatch(String inFile, String templateFile, String outFile, int match_method) {

		System.err.println(
				"[LOG]\tLoading library " + Core.NATIVE_LIBRARY_NAME + " using image recognition algorithm TM_CCOEFF_NORMED");

		System.out.println("[LOG]\tSearching matches of " + templateFile + " in " + inFile);

		Mat img = Highgui.imread(inFile);
		Mat templ = Highgui.imread(templateFile);

		// / Create the result matrix
		int result_cols = img.cols() - templ.cols() + 1;
		int result_rows = img.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

		// / Do the Matching and Normalize
		Imgproc.matchTemplate(img, templ, result, match_method);
		Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

		// TODO: manage the multiple matches!
		// Checks whether the match is univocal
		List<Point> matches = new LinkedList<Point>();

		for (int i = 0; i < result_rows; i++) {
			for (int j = 0; j < result_cols; j++) {
				if (result.get(i, j)[0] >= 0.99)
					matches.add(new Point(i, j));
				// System.out.println("("+i+", "+j+"): " + result.get(i, j)[0]);
			}
		}

		if (matches.size() == 0) {
			System.err.println("[WARNING]\tOpenCv did not find any matches!");
			return false;
		} else if (matches.size() > 1) {
			System.err.println("[WARNING]\tOpenCv found multiple matches!");
			return false;
		}

		// / Localizing the best match with minMaxLoc
		MinMaxLocResult mmr = Core.minMaxLoc(result);

		Point matchLoc;
		if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
			matchLoc = mmr.minLoc;
		} else {
			matchLoc = mmr.maxLoc;
		}

		// / Show me what you got
		Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(), matchLoc.y + templ.rows()),
				new Scalar(0, 255, 0), 2);

		// Save the visualized detection.
		// System.out.println("Writing "+ outFile);
		// Highgui.imwrite(outFile, templ);
		return true;
	}

}
