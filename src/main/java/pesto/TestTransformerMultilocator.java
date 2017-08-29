package pesto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TestTransformerMultilocator {

	public static void main(String[] args) throws Exception {
		new TestTransformerMultilocator().TestTransformation();
	}
	public void TestTransformation() throws IOException {

		final File folder = new File("src/testSuite");

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isFile()) {

				String s;
				BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
				
				PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(fileEntry+"_Temp", false)));
				

				while ((s = reader.readLine()) != null)
				{
					System.out.println(s);
					if (s.contains("poMultilocator."))
					{
						s=s.replace("poMultilocator.", "poSelenium.");
					}
					else if (s.contains("import poSelenium."))
					{
						s=s.replace("poSelenium.", "poMultilocator.");
					}
					else if(s.contains("import poSikuli.")){
						s=s.replace("poSikuli.", "poMultilocator.");
					}
					writer.println(s);
					
				}

				reader.close();
				writer.close();
				
				reader = new BufferedReader(new FileReader(fileEntry+"_Temp"));
				writer = new PrintWriter(new BufferedWriter(new FileWriter(fileEntry, false)));
				while ((s = reader.readLine()) != null)
				{
					System.out.println(s);
					writer.println(s);
					
				}
				reader.close();
				writer.close();
				
				File file = new File(fileEntry+"_Temp");
				file.delete();

			}
		}

	}

}
