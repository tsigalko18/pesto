package pesto;

public class PestoMain {

	public static void main(String[] args) throws Exception {

		/* Step 1: run the test suite to gather the visual locators. */
		TestSuiteRunner runner = new TestSuiteRunner("testSuite.DemoSeleniumTestSuite");
		runner.run();
		
		/* Step 2: transform the page objects. */
		PageObjectTransformer poTranformer = new PageObjectTransformer();
		poTranformer.run();

		/* Step 3: transform the test cases. */
		TestTransformer testTranformer = new TestTransformer();
		testTranformer.run();
	}

}
