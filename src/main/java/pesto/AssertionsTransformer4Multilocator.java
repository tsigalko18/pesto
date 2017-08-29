package pesto;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.MarkerAnnotationExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class AssertionsTransformer4Multilocator {

	private final static String poFolder = "src/poToTransform/";
	private final static String poFolderMultilocator = "src/poMultilocator/";
	//protected static Map<String, String> list = new HashMap<String, String>();
	protected static Map<String, LinkedList<String>> list = new HashMap<String, LinkedList<String>>();
	public static CompilationUnit cu;
	public static String locatorsFileAsString = "";

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		new AssertionsTransformer4Multilocator().parsePageObject(null);
	}

	public static void parsePageObject(Map<String, LinkedList<String>> l) throws ParseException, IOException {

		String lfn = "screenshots/locators.txt";
		File locFile = new File(lfn);
		locatorsFileAsString = FileUtils.readFileToString(locFile);
		
		// reading locators
		System.out.print("Reading locators.txt...");
		BufferedReader inloc = new BufferedReader(new FileReader(locFile));
		int read = 0;
		while (inloc.ready()) {
			String s = inloc.readLine();
			String multi = s.split(" -> ")[0];
			if(StringUtils.countMatches(locatorsFileAsString, multi) > 1){
				if(list.get(s.split(" -> ")[0]) == null) { 
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
		
		System.out.print("\tFound " + read + " locators!");
		
		System.out.println(" OK!");

		System.out.print("Reading page objects...");
		File[] files = new File(poFolder).listFiles();
		FileInputStream in = null;
		System.out.print(" Found " + files.length + " page objects!");
		System.out.println(" OK!\n");
		// foreach page object calls the ast transformer
		for (File file : files) {
			if (!file.getName().contains(".java")) {
				continue;
			} else {
				// creates an input stream for the file to be parsed
				in = new FileInputStream(poFolder + file.getName());
				try {
					// parse the file
					cu = JavaParser.parse(in);
				} finally {
					in.close();
				}
			}

			System.out.print("Transforming old " + file.getName() + " ...\n");

			// modifies the ast
			new PackageVisitor().visit(cu, null);
			addMultilocatorImports();

			//new ClassVisitor().visit(cu, null);
			new FieldDeclarationVisitor().visit(cu, null);

			System.out.println(" OK!");

			System.out.print("Saving new " + file.getName() + " ...");
			// save the modified AST to a new file
			String source = cu.toString();
			File fileMod = new File("src/poMultilocator/" + file.getName());
			FileUtils.writeStringToFile(fileMod, source);
			System.out.println(" OK!\n");
		}
	}
	
	private static void addMultilocatorImports() {
		cu.getImports().add(new ImportDeclaration(new NameExpr("org.openqa.selenium.support.FindAll"), false, false));
	}

	/**
	 * Simple visitor implementation for visiting package nodes.
	 */
	private static class PackageVisitor extends VoidVisitorAdapter {

		public void visit(PackageDeclaration p, Object arg) {
			p.setName(new NameExpr("poMultilocator"));
		}
	}

	/**
	 * Simple visitor implementation for visiting import nodes.
	 */
	private static class ImportVisitor extends VoidVisitorAdapter {

		public void visit(ImportDeclaration i, Object arg) {
			System.out.println(i.getName());
		}
	}

	/**
	 * Simple visitor implementation for visiting FieldDeclarationVisitor nodes.
	 */
	private static class FieldDeclarationVisitor extends VoidVisitorAdapter {

		public void visit(FieldDeclaration f, Object arg) {
			//	
			
			if (f.getAnnotations() != null) {
				
				String varToRetrieve = f.getAnnotations().get(0).toString();
				
				// retrieves the locator value
				if(varToRetrieve.contains("text()")){
					varToRetrieve = varToRetrieve.replace("@FindBy(", "");
					varToRetrieve = varToRetrieve.replace("\")", "\"");
					varToRetrieve = varToRetrieve.replace("xpath = ", "xpath=");
				} else {
					varToRetrieve = varToRetrieve.replace("@FindBy(", "");
					varToRetrieve = varToRetrieve.replace(")", "");
					varToRetrieve = varToRetrieve.replace("xpath = ", "xpath=");
				}
				
				varToRetrieve = varToRetrieve.replace("xpath=", "");
				varToRetrieve = varToRetrieve.replace("\"", "");
				
				// read the multilocator file
				String lfn = "Multilocator-AddressBook.txt";
				File multilocFile = new File(lfn);
				
				// reading multilocators
				System.out.println("Reading multilocators.txt...");
				BufferedReader multibr;
				try {
					String multilocatorsFileAsString;
					multilocatorsFileAsString = FileUtils.readFileToString(multilocFile);
					multibr = new BufferedReader(new FileReader(multilocFile));
					int read = 0;
					while (multibr.ready()) {
						String s = multibr.readLine();
						
						if(s.contains("countAllBroken")){
							continue;
						}
						else if(s.contains(varToRetrieve)){		
							
							String multilocator = "FindAll({\n";
							String[] multilocatorLine = s.split("\t");
							
							multilocator = multilocator + "\t\t@FindBy(xpath=\"" + multilocatorLine[2] + "\"),\n";
							multilocator = multilocator + "\t\t@FindBy(xpath=\"" + multilocatorLine[8] + "\"),\n";
							multilocator = multilocator + "\t\t@FindBy(xpath=\"" + multilocatorLine[12].replace("\"", "\'") + "\"),\n";
							multilocator = multilocator + "\t\t@FindBy(xpath=\"" + multilocatorLine[16].replace("\"", "\'") + "\"),\n";
							multilocator = multilocator + "\t\t@FindBy(xpath=\"" + multilocatorLine[20] + "\")\n\t})";
							
							MarkerAnnotationExpr marker = new MarkerAnnotationExpr(new NameExpr(multilocator));
							f.getAnnotations().set(0, marker);
							
							return;
						}
						else {
							read++;
						}
						
//						System.out.println(multilocatorLine[0] + "\n" + multilocatorLine[3] + " " + multilocatorLine[4] + "\n"
//								+ multilocatorLine[7] + " " + multilocatorLine[8] + "\n"
//								+ multilocatorLine[11] + " " + multilocatorLine[12] + "\n"
//								+ multilocatorLine[15] + " " + multilocatorLine[16] + "\n"
//								+ multilocatorLine[19] + " " + multilocatorLine[20] + "\n"
//								);
					}
					multibr.close();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
		}
	}

	/**
	 * Simple visitor implementation for visiting class nodes.
	 */
	private static class ClassVisitor extends VoidVisitorAdapter {

		public void visit(ClassOrInterfaceDeclaration c, Object arg) {

			// counts the number of FieldDeclarations
			int f1 = 0;
			for (BodyDeclaration b : c.getMembers())
				if (b instanceof FieldDeclaration)
					f1++;

			/* ***********************
			 * METHODS' TRANSFORMATION
			 * ***********************
			 */
			changeMethods(cu);

			/* *************************************
			 * CREATION OF SIKULI SPECIFIC VARIABLES
			 * *************************************
			 */
			int nuovi = f1;
			
			// Variable screen
			VariableDeclarator screen = new VariableDeclarator();
			screen.setId(new VariableDeclaratorId("screen"));
			// screen.setInit(new NameExpr("new DesktopScreenRegion()"));
			FieldDeclaration s = ASTHelper.createFieldDeclaration(ModifierSet.PRIVATE,
					ASTHelper.createReferenceType("ScreenRegion", 0), screen);
			c.getMembers().add(nuovi, s);
			nuovi++;

			// Variable ris
			VariableDeclarator ris = new VariableDeclarator();
			ris.setId(new VariableDeclaratorId("ris"));
			c.getMembers().add(
					nuovi,
					ASTHelper.createFieldDeclaration(Modifier.PRIVATE,
							ASTHelper.createReferenceType("ScreenRegion", 0), ris));
			nuovi++;

			// Variable keyboard
			VariableDeclarator keyboard = new VariableDeclarator();
			keyboard.setId(new VariableDeclaratorId("keyboard"));
			c.getMembers().add(
					nuovi,
					ASTHelper.createFieldDeclaration(Modifier.PRIVATE, ASTHelper.createReferenceType("Keyboard", 0),
							keyboard));
			nuovi++;

			// Variable mouse
			VariableDeclarator mouse = new VariableDeclarator();
			mouse.setId(new VariableDeclaratorId("mouse"));
			c.getMembers()
					.add(nuovi,
							ASTHelper.createFieldDeclaration(Modifier.PRIVATE,
									ASTHelper.createReferenceType("Mouse", 0), mouse));
			nuovi++;

			// Variable urlBase (now empty)
			VariableDeclarator urlBaseVar = new VariableDeclarator();
			urlBaseVar.setId(new VariableDeclaratorId("urlBase"));
			urlBaseVar.setInit(new StringLiteralExpr(""));

			int ms = ModifierSet.PROTECTED;

			FieldDeclaration urlBase = ASTHelper.createFieldDeclaration(ms, ASTHelper.createReferenceType("String", 0),
					urlBaseVar);
			c.getMembers().add(nuovi, urlBase);

			/* *****************************
			 * CREATION OF CLASS CONSTRUCTOR 
			 * *****************************
			 */
			MethodDeclaration constructor = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.createReferenceType(
					c.getName(), 0), "");
			// add WebDriver parameter to the constructor
			Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("WebDriver", 0), "driver");
			param.setVarArgs(false);
			ASTHelper.addParameter(constructor, param);

			// add a body to the method
			BlockStmt block = new BlockStmt();
			constructor.setBody(block);

			/* *****************************
			 * ADDITION OF VISUAL STATEMENTS
			 * *****************************
			 */
			ASTHelper.addStmt(block, new NameExpr("this.driver = driver"));
			ASTHelper.addStmt(block, new NameExpr("PageFactory.initElements(driver, this)"));
			ASTHelper.addStmt(block, new NameExpr("screen = new DesktopScreenRegion()"));
			ASTHelper.addStmt(block, new NameExpr("ris = new DesktopScreenRegion()"));
			ASTHelper.addStmt(block, new NameExpr("screen = new DesktopScreenRegion()"));
			ASTHelper.addStmt(block, new NameExpr("mouse = new DesktopMouse()"));
			ASTHelper.addStmt(block, new NameExpr("keyboard = new DesktopKeyboard()"));

			/* ****************************
			 * ADDITION OF VISUAL LOCATORS
			 * ****************************
			 */
			Iterator iterator = list.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				LinkedList<String> value = list.get(key);
				
				for (BodyDeclaration b : c.getMembers()) {
					if (b instanceof FieldDeclaration) {
						if (b.getAnnotations() != null) {
							String nomeVar = ((FieldDeclaration) b).getVariables().get(0).toString();
							String varToRetrieve = "";
							
							if(((FieldDeclaration) b).getAnnotations().get(0).toString().contains("text()")){
								varToRetrieve = ((FieldDeclaration) b).getAnnotations().get(0).toString();
								varToRetrieve = varToRetrieve.replace("@FindBy(", "");
								varToRetrieve = varToRetrieve.replace("\")", "\"");
								varToRetrieve = varToRetrieve.replace("xpath = ", "xpath=");
							} else {
								varToRetrieve = ((FieldDeclaration) b).getAnnotations().get(0).toString().replaceAll(" ", "");
								varToRetrieve = varToRetrieve.replace("@FindBy(", "");
								varToRetrieve = varToRetrieve.replace(")", "");
							}
							
							int multiOrNot = StringUtils.countMatches(locatorsFileAsString, key);
							
							String[] parts = key.split("-");
							String po = parts[0];
							String we = parts[1];
							
							if(multiOrNot == 1){
								if (po.equals(c.getName())) {
									if (varToRetrieve.equals(we)) {
										//System.err.println("\n"+varToRetrieve + "\n" + po + "\n" + we);
										String ne = nomeVar + " = " + "new ImageTarget(new File(urlBase+\"" + value.get(0)
												+ "\"))";
										ASTHelper.addStmt(block, new NameExpr(ne));
									}
								} else {
									continue;
								}
							} else if(multiOrNot > 1) {
								if (po.equals(c.getName())) {
									if (varToRetrieve.equals(we)) {
										String ne = nomeVar + " = " + "new MultiStateTarget()";
										ASTHelper.addStmt(block, new NameExpr(ne));
										
										for(int k = 0; k < value.size(); k++){
											ne = nomeVar +  ".addState(new ImageTarget(new File(urlBase+\"" + value.get(k)
												+ "\")), \""+ value.get(k) + "\")";
											ASTHelper.addStmt(block, new NameExpr(ne));
										}
										
										// address.addState(new ImageTarget(new File("locked.png")), "locked");
										//ne = nomeVar +  ".addState(new ImageTarget(new File(urlBase" + ")), \"random\")";
										//ASTHelper.addStmt(block, new NameExpr(ne));
									}
								} else {
									continue;
								}
							}
						}
					}
				}
			}

			// Add the expected exceptions
			List<NameExpr> throws_ = new LinkedList<NameExpr>();
			throws_.add(new NameExpr("MalformedURLException"));

			constructor.setThrows(throws_);

			c.getMembers().add(++nuovi, constructor);
		}
	}

	private static void changeMethods(CompilationUnit cu) {
		List<TypeDeclaration> types = cu.getTypes();
		for (TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			for (BodyDeclaration member : members) {
				if (member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					changeMethod(method);
				}
			}
		}
	}

	private static void changeMethod(MethodDeclaration n) {

		BlockStmt nuovoBlocco = new BlockStmt();
		Iterator<Statement> i = n.getBody().getStmts().iterator();

		while (i.hasNext()) {
			Statement o = i.next();

			if (o.toString().contains("click") || o.toString().contains("sendKeys") || o.toString().contains("clear")
					|| o.toString().contains("getText")) {

				String variabile = o.toString().substring(0, o.toString().indexOf('.'));
				int space = variabile.lastIndexOf(" ");
				if (space != -1) 	variabile =  variabile.substring(space+1,variabile.length());
				String metodo = o.toString().substring(o.toString().indexOf('.') + 1, o.toString().indexOf('('));
				String parametro = o.toString().substring(o.toString().indexOf('(') + 1, o.toString().indexOf(')'));

				if (metodo.equals("click")) {
					ASTHelper.addStmt(nuovoBlocco, new NameExpr(metodo + "(" + variabile + ")"));
				} else if (metodo.equals("sendKeys")) {
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("type(" + variabile + ", " + parametro + ")"));
				} else if (metodo.equals("clear")) {
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("clear(" + variabile + ")"));
				} else if (metodo.equals("getText")) {
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("screen.setScore(1.00)"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("ris = screen.find("+variabile+")"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("Canvas canvas = new DesktopCanvas()"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("canvas.addBox(ris).display(1)"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("canvas.addLabel(ris, \"Evaluating Assertion!\").display(1)"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("if (ris==null) return false;\n\t\telse return true"));
				}
			}
			else if(o.toString().contains("selectByVisibleText")){
				// new Select(bday).selectByVisibleText(day); -->  typeSelect(bday, day);
				String parametro = o.toString().substring(12, o.toString().indexOf(')', 0));
				String locator = o.toString().substring(o.toString().indexOf('(') + 1, o.toString().indexOf(')'));
				
				ASTHelper.addStmt(nuovoBlocco, new NameExpr("typeSelect("+locator+", "+parametro+")"));
			}
			else {
				ASTHelper.addStmt(nuovoBlocco, o);
			}

		}
		List<NameExpr> throws_ = new LinkedList<NameExpr>();
		throws_.add(new NameExpr("MalformedURLException"));
		throws_.add(new NameExpr("InterruptedException"));

		n.setBody(nuovoBlocco);
		n.setThrows(throws_);
	}
	
	}
}