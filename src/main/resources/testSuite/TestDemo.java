package testSuite;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import poSelenium.IndexPage;

public class TestDemo extends BaseTest {

	@Test
	public void test() throws InterruptedException, IOException {
		IndexPage ip = new IndexPage();
		ip.goToHome();
		assertTrue(ip.getPageTitle("Welcome to the Software Engineering"));
	}

}
