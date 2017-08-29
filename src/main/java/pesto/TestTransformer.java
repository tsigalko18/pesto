package pesto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TestTransformer {

	public void run() throws IOException {

		final File folder = new File(Settings.TESTSUITE_FOLDER);

		System.out.print("[LOG]\tRunning Test Transformer ");

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {

				String s;
				BufferedReader reader = new BufferedReader(new FileReader(fileEntry));

				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileEntry + "_Temp", false)));

				while ((s = reader.readLine()) != null) {
					// System.out.println(s);
					if (s.contains("poSikuli.")) {
						System.out.println("from Sikuli to Selenium");
						s = s.replace("poSikuli.", "poSelenium.");
					} else if (s.contains("import poSelenium.")) {
						System.out.println("from Selenium to Sikuli");
						s = s.replace("poSelenium.", "poSikuli.");
					}
					writer.println(s);
				}

				reader.close();
				writer.close();

				reader = new BufferedReader(new FileReader(fileEntry + "_Temp"));
				writer = new PrintWriter(new BufferedWriter(new FileWriter(fileEntry, false)));
				while ((s = reader.readLine()) != null) {
					// System.out.println(s);
					writer.println(s);
				}
				reader.close();
				writer.close();

				File file = new File(fileEntry + "_Temp");
				file.delete();

			}

		}
		System.out.println("[LOG]\tTests Trasformation Complete");
	}

}
