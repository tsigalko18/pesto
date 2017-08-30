package pesto;

import java.io.IOException;

import org.junit.runner.JUnitCore;

public class TestSuiteRunner {

	String suite;

	public TestSuiteRunner(String s) {
		suite = s;
	}

	public void run() throws IOException {

		String classRunner = "testSuite.DemoSeleniumTestSuite";

		System.out.println("[LOG]\tRunning Test Suite " + classRunner);
		try {
			JUnitCore.runClasses(Class.forName(classRunner));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
}
