package pesto;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class PestoMain {

	public static void main(String[] args) throws Exception {

		/* Step 1: run the test suite to gather the visual locators. */
//		String classRunner = "testSuite.DemoSeleniumTestSuite";
//
//		try {
//			System.out.println("[LOG]\tRunning Test Suite " + classRunner);
//			Result result = JUnitCore.runClasses(Class.forName(classRunner));
//			System.out.println(result.wasSuccessful());
//		} catch (ClassNotFoundException e) {
//			e.printStackTrace();
//		}

		/* Step 2: transform the page objects. */
		PageObjectTransformer poTranformer = new PageObjectTransformer();
		poTranformer.run();

		/* Step 3: transform the test cases. */
//		TestTransformer testTranformer = new TestTransformer();
//		testTranformer.run();
	}

}
