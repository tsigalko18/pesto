package pesto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import utils.UtilsCodeTransformation;

public class PageObjectTransformer {

	static String poFolder;
	static String poSikuliFolder;
	static File locFile;
	static String locatorsFileAsString;
	static Map<String, LinkedList<String>> list = new HashMap<String, LinkedList<String>>();

	public PageObjectTransformer() {
		poFolder = Settings.PO_SELENIUM_FOLDER;
		poSikuliFolder = Settings.PO_SIKULI_FOLDER;
		locFile = new File(Settings.LOCATORS_FILE);
		try {
			locatorsFileAsString = FileUtils.readFileToString(locFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * run the code transformation of the Selenium Page Objects
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		System.out.println("[LOG]\tRunning Page Object Transformer");

		readVisualLocators();

		transformPageObjects();

		System.out.println("[LOG]\tPage Objects Trasformation Complete");
	}

	private static void readVisualLocators() throws IOException {

		System.out.println("[LOG]\tReading " + locFile.getName());

		BufferedReader inloc = new BufferedReader(new FileReader(locFile));
		int read = 0;
		while (inloc.ready()) {
			String s = inloc.readLine();
			String multi = s.split(" -> ")[0];
			if (StringUtils.countMatches(locatorsFileAsString, multi) > 1) {
				if (list.get(s.split(" -> ")[0]) == null) {
					LinkedList<String> listWithOneElement = new LinkedList<String>();
					listWithOneElement.add(s.split(" -> ")[1]);
					list.put(s.split(" -> ")[0], listWithOneElement);
					continue;
				} else {
					list.get(s.split(" -> ")[0]).add(s.split(" -> ")[1]);
					list.put(s.split(" -> ")[0], list.get(s.split(" -> ")[0]));
				}
			} else {
				LinkedList<String> listWithOneElement = new LinkedList<String>();
				listWithOneElement.add(s.split(" -> ")[1]);
				list.put(s.split(" -> ")[0], listWithOneElement);
			}
			read++;
		}
		inloc.close();

		System.out.println("[LOG]\tFound " + read + " locators in " + locFile.getName());
	}

	private static void transformPageObjects() throws IOException, ParseException {

		System.out.println("[LOG]\tSearching for Selenium page objects in " + poFolder);

		File[] files = new File(poFolder).listFiles();
		FileInputStream in = null;

		System.out.println("[LOG]\tFound " + files.length + " Selenium page objects in " + poFolder);

		for (File file : files) {

			/*
			 * ************** // STEP 1 PARSE **************
			 */
			CompilationUnit cu;

			if (!file.getName().contains(".java")) {
				continue;
			} else {
				cu = parsePageObjectIntoCompilationUnit(file);
			}

			/*
			 * ************** // STEP 2 TRANSFORM **************
			 */

			System.out.println("[LOG]\tTransforming page object " + file.getName());

			modifyPageObject(cu);

			/*
			 * ************** // STEP 3 SAVE **************
			 */

			System.out.println("[LOG]\tSaving new " + file.getName() + " in " + poSikuliFolder);

			savePageObjectsOnFileSystem(cu, file);

		}
	}

	private static CompilationUnit parsePageObjectIntoCompilationUnit(File file) throws IOException, ParseException {

		// creates an input stream for the file to be parsed
		FileInputStream in = new FileInputStream(poFolder + file.getName());
		try {
			// parse the file
			CompilationUnit cu = JavaParser.parse(in);
			return cu;
		} finally {
			in.close();
		}
	}

	private static void modifyPageObject(CompilationUnit cu) {

		// modifies the AST
		UtilsCodeTransformation.changePackage(cu);
		UtilsCodeTransformation.addSikuliImports(cu);
		UtilsCodeTransformation.changeClass(cu, list, locatorsFileAsString);
		UtilsCodeTransformation.changeFields(cu, locatorsFileAsString);
		UtilsCodeTransformation.removeUselessMethods(cu);

	}

	private static void savePageObjectsOnFileSystem(CompilationUnit cu, File file) throws IOException {

		String source = cu.toString();
		File fileMod = new File(poSikuliFolder + file.getName());
		FileUtils.writeStringToFile(fileMod, source);
	}

}
