package utils;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import japa.parser.ASTHelper;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.BodyDeclaration;
import japa.parser.ast.body.ClassOrInterfaceDeclaration;
import japa.parser.ast.body.ConstructorDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.ModifierSet;
import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.StringLiteralExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.VoidVisitorAdapter;

public class UtilsCodeTransformation {

	protected static String locatorsFileAsString;

	/**
	 * Modifies the Java package declaration to Sikuli's
	 * 
	 * @param cu
	 */
	public static void changePackage(CompilationUnit cu) {
		new PackageVisitor().visit(cu, null);
	}

	/**
	 * Simple visitor implementation for visiting package nodes.
	 */
	private static class PackageVisitor extends VoidVisitorAdapter<Object> {

		public void visit(PackageDeclaration p, Object arg) {
			p.setName(new NameExpr("poSikuli"));
		}
	}

	/**
	 * Imports the correct Sikuli libraries imports declarations
	 * 
	 * @param cu
	 */
	public static void addSikuliImports(CompilationUnit cu) {
		cu.getImports().add(new ImportDeclaration(new NameExpr("java.net.URL"), false, false));
		cu.getImports().add(new ImportDeclaration(new NameExpr("java.io.File"), false, false));
		cu.getImports().add(new ImportDeclaration(new NameExpr("java.net.MalformedURLException"), false, false));
		cu.getImports().add(new ImportDeclaration(new NameExpr("org.sikuli.api"), false, true));
		cu.getImports().add(new ImportDeclaration(new NameExpr("org.sikuli.api.robot"), false, true));
		cu.getImports().add(new ImportDeclaration(new NameExpr("org.sikuli.api.robot.Keyboard"), false, false));
		cu.getImports().add(new ImportDeclaration(new NameExpr("org.sikuli.api.visual"), false, true));
		cu.getImports().add(new ImportDeclaration(new NameExpr("org.sikuli.api.robot.desktop"), false, true));
	}

	/**
	 * Modifies the main Java class
	 * 
	 * @param cu
	 * @param list
	 * @param l
	 */
	public static void changeClass(CompilationUnit cu, Map<String, LinkedList<String>> list, String l) {
		ClassVisitor cv = new ClassVisitor(cu, list, l);
		cv.visit(cu, null);
	}

	/**
	 * Simple visitor implementation for visiting class nodes.
	 */
	private static class ClassVisitor extends VoidVisitorAdapter<Object> {

		protected Map<String, LinkedList<String>> ls = new HashMap<String, LinkedList<String>>();
		// protected String locatorsFileAsString;

		public ClassVisitor(CompilationUnit cu, Map<String, LinkedList<String>> map, String l) {
			ls = map;
			locatorsFileAsString = l;
		}

		public void visit(ClassOrInterfaceDeclaration c, Object arg) {

			// counts the number of FieldDeclarations
			int f1 = 0;
			for (BodyDeclaration b : c.getMembers())
				if (b instanceof FieldDeclaration)
					f1++;

			for (BodyDeclaration m : c.getMembers()) {
				if (m instanceof MethodDeclaration) {
					MethodDeclaration meth = (MethodDeclaration) m;
					changeMethod(meth);
				}
			}

			/*
			 * *********************** METHODS' TRANSFORMATION ***********************
			 */

			// changeMethods(compilationUnit);

			/*
			 * ************************************* CREATION OF SIKULI SPECIFIC VARIABLES
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
			c.getMembers().add(nuovi, ASTHelper.createFieldDeclaration(Modifier.PRIVATE,
					ASTHelper.createReferenceType("ScreenRegion", 0), ris));
			nuovi++;

			// Variable keyboard
			VariableDeclarator keyboard = new VariableDeclarator();
			keyboard.setId(new VariableDeclaratorId("keyboard"));
			c.getMembers().add(nuovi, ASTHelper.createFieldDeclaration(Modifier.PRIVATE,
					ASTHelper.createReferenceType("Keyboard", 0), keyboard));
			nuovi++;

			// Variable mouse
			VariableDeclarator mouse = new VariableDeclarator();
			mouse.setId(new VariableDeclaratorId("mouse"));
			c.getMembers().add(nuovi, ASTHelper.createFieldDeclaration(Modifier.PRIVATE,
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

			/*
			 * ***************************** CREATION OF CLASS CONSTRUCTOR
			 * *****************************
			 */
			MethodDeclaration constructor = new MethodDeclaration(ModifierSet.PUBLIC,
					ASTHelper.createReferenceType(c.getName(), 0), "");
			// add WebDriver parameter to the constructor
			Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("WebDriver", 0), "driver");
			param.setVarArgs(false);
			ASTHelper.addParameter(constructor, param);

			// add a body to the method
			BlockStmt block = new BlockStmt();
			constructor.setBody(block);

			/*
			 * ***************************** CONSTRUCTOR: ADDITION OF VISUAL STATEMENTS
			 * *****************************
			 */
			ASTHelper.addStmt(block, new NameExpr("this.driver = driver"));
			ASTHelper.addStmt(block, new NameExpr("PageFactory.initElements(driver, this)"));
			ASTHelper.addStmt(block, new NameExpr("screen = new DesktopScreenRegion()"));
			ASTHelper.addStmt(block, new NameExpr("ris = new DesktopScreenRegion()"));
			ASTHelper.addStmt(block, new NameExpr("screen = new DesktopScreenRegion()"));
			ASTHelper.addStmt(block, new NameExpr("mouse = new DesktopMouse()"));
			ASTHelper.addStmt(block, new NameExpr("keyboard = new DesktopKeyboard()"));

			/*
			 * **************************** CONSTRUCTOR: ADDITION OF VISUAL LOCATORS
			 * ****************************
			 */
			Iterator<String> iterator = ls.keySet().iterator();
			while (iterator.hasNext()) {
				String key = iterator.next().toString();
				LinkedList<String> value = ls.get(key);

				for (BodyDeclaration b : c.getMembers()) {
					if (b instanceof FieldDeclaration) {
						if (b.getAnnotations() != null) {
							String nomeVar = ((FieldDeclaration) b).getVariables().get(0).toString();
							String varToRetrieve = "";

							if (((FieldDeclaration) b).getAnnotations().get(0).toString().contains("text()")) {
								varToRetrieve = ((FieldDeclaration) b).getAnnotations().get(0).toString();
								varToRetrieve = varToRetrieve.replace("@FindBy(", "");
								varToRetrieve = varToRetrieve.replace("\")", "\"");
								varToRetrieve = varToRetrieve.replace("xpath = ", "xpath=");
							} else {
								varToRetrieve = ((FieldDeclaration) b).getAnnotations().get(0).toString()
										.replaceAll(" ", "");
								varToRetrieve = varToRetrieve.replace("@FindBy(", "");
								varToRetrieve = varToRetrieve.replace(")", "");
							}

							int multiOrNot = StringUtils.countMatches(locatorsFileAsString, key);

							String[] parts = key.split("-");
							String po = parts[0];
							String we = parts[1];

							if (multiOrNot == 1) {
								if (po.equals(c.getName())) {
									if (varToRetrieve.equals(we)) {
										// System.err.println("\n"+varToRetrieve + "\n" + po + "\n" + we);
										String ne = nomeVar + " = " + "new ImageTarget(new File(urlBase+\""
												+ value.get(0) + "\"))";
										ASTHelper.addStmt(block, new NameExpr(ne));
									}
								} else {
									continue;
								}
							} else if (multiOrNot > 1) {
								if (po.equals(c.getName())) {
									if (varToRetrieve.equals(we)) {
										String ne = nomeVar + " = " + "new MultiStateTarget()";
										ASTHelper.addStmt(block, new NameExpr(ne));

										for (int k = 0; k < value.size(); k++) {
											ne = nomeVar + ".addState(new ImageTarget(new File(urlBase+\""
													+ value.get(k) + "\")), \"" + value.get(k) + "\")";
											ASTHelper.addStmt(block, new NameExpr(ne));
										}
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

			/*
			 * *************************** ADDITION OF VISUAL METHODS
			 * ***************************
			 */
			c.getMembers().add(createVisualMethodClick());
			c.getMembers().add(createVisualMethodType());
			c.getMembers().add(createVisualMethodClear());
			c.getMembers().add(createTypeSelect());

			// c.getMembers().add(createVisualMethodPaste());
			// c.getMembers().add(createVisualMethodMenuSelect());
			// c.getMembers().add(createVisualMethodWheel());
			// c.getMembers().add(createVisualMethodIsElementPresent());
			// c.getMembers().add(createVisualTypeRelative());
			// c.getMembers().add(createVisualClearRelative());
			// c.getMembers().add(createVisualMenuSelectRelative());
		}
	}

	/**
	 * Modifies the Web Elements and Annotations
	 * 
	 * @param cu
	 * @param list
	 * @param locatorsFileAsString
	 */
	public static void changeFields(CompilationUnit cu, String locatorsFileAsString) {
		FieldDeclarationVisitor fv = new FieldDeclarationVisitor(cu, locatorsFileAsString);
		fv.visit(cu, null);
	}

	/**
	 * Simple visitor implementation for visiting FieldDeclarationVisitor nodes.
	 */
	private static class FieldDeclarationVisitor extends VoidVisitorAdapter {

		CompilationUnit c;

		public FieldDeclarationVisitor(CompilationUnit cu, String l) {
			c = cu;
			locatorsFileAsString = l;
		}

		public void visit(FieldDeclaration f, Object arg) {

			String aKey = c.getTypes().get(0).getName() + "-";

			if (f.getAnnotations() != null) {

				String varToRetrieve = f.getAnnotations().get(0).toString();

				if (varToRetrieve.contains("text()")) {
					varToRetrieve = varToRetrieve.replace("@FindBy(", "");
					varToRetrieve = varToRetrieve.replace("\")", "\"");
					varToRetrieve = varToRetrieve.replace("xpath = ", "xpath=");
				} else {
					varToRetrieve = varToRetrieve.replace("@FindBy(", "");
					varToRetrieve = varToRetrieve.replace(")", "");
					varToRetrieve = varToRetrieve.replace("xpath = ", "xpath=");
				}

				varToRetrieve = aKey + varToRetrieve;
				int multiOrNot = StringUtils.countMatches(locatorsFileAsString, varToRetrieve);

				if (multiOrNot > 1) {
					if (f.getType().toString().equals("WebElement")) {
						f.setType(ASTHelper.createReferenceType("MultiStateTarget", 0));
						f.setAnnotations(null);
					}
				} else { // if(multiOrNot == 1){
					if (f.getType().toString().equals("WebElement")) {
						f.setType(ASTHelper.createReferenceType("Target", 0));
						f.setAnnotations(null);
					}
				}
			} else {
				if (f.getType().toString().equals("WebElement")) {
					f.setType(ASTHelper.createReferenceType("Target", 0));
				}
			}
		}

	}

	/**
	 * Inside MethodDeclaration n, modifies Selenium commands to Sikuli's
	 * 
	 * @param n
	 */
	private static void changeMethod(MethodDeclaration n) {

		BlockStmt nuovoBlocco = new BlockStmt();
		Iterator<Statement> i = n.getBody().getStmts().iterator();

		while (i.hasNext()) {
			Statement o = i.next();

			if (o.toString().contains("click") || o.toString().contains("sendKeys") || o.toString().contains("clear")
					|| o.toString().contains("getText")) {

				String variabile = o.toString().substring(0, o.toString().indexOf('.'));
				int space = variabile.lastIndexOf(" ");
				if (space != -1)
					variabile = variabile.substring(space + 1, variabile.length());
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
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("ris = screen.find(" + variabile + ")"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("Canvas canvas = new DesktopCanvas()"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("canvas.addBox(ris).display(1)"));
					ASTHelper.addStmt(nuovoBlocco,
							new NameExpr("canvas.addLabel(ris, \"Evaluating Assertion!\").display(1)"));
					ASTHelper.addStmt(nuovoBlocco, new NameExpr("if (ris==null) return false;\n\t\telse return true"));
				}
			} else if (o.toString().contains("selectByVisibleText")) {
				// new Select(bday).selectByVisibleText(day); --> typeSelect(bday, day);
				String parametro = o.toString().substring(12, o.toString().indexOf(')', 0));
				String locator = o.toString().substring(o.toString().indexOf('(') + 1, o.toString().indexOf(')'));

				ASTHelper.addStmt(nuovoBlocco, new NameExpr("typeSelect(" + locator + ", " + parametro + ")"));
			} else {
				ASTHelper.addStmt(nuovoBlocco, o);
			}

		}
		List<NameExpr> throws_ = new LinkedList<NameExpr>();
		throws_.add(new NameExpr("MalformedURLException"));
		throws_.add(new NameExpr("InterruptedException"));

		n.setBody(nuovoBlocco);
		n.setThrows(throws_);
	}

	/**
	 * Creates a stub method to manage Select commands with the visual technology of
	 * Sikuli
	 * 
	 * @return
	 */
	private static MethodDeclaration createTypeSelect() {
		// create the method
		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "typeSelect");
		// add a parameter to the method
		Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("Target", 0), "element");
		param.setVarArgs(false);
		Parameter param2 = ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "value");
		param2.setVarArgs(false);
		ASTHelper.addParameter(method, param);
		ASTHelper.addParameter(method, param2);

		// add a body to the method
		BlockStmt block = new BlockStmt();
		method.setBody(block);

		List<NameExpr> throws_ = new LinkedList<NameExpr>();
		throws_.add(new NameExpr("InterruptedException"));
		method.setThrows(throws_);

		ASTHelper.addStmt(block, new NameExpr("int v = 0;\n\n" + "\t\ttry { \n"
				+ "\t\t\tv = Integer.parseInt(value); \n" + "\t\t} catch (NumberFormatException e) {\n"
				+ "\t\t\tScreenRegion ris = screen.find(element);\n" + "\t\t\tmouse.click(ris.getCenter());\n"
				+ "\t\t\tkeyboard.type(value);\n" + "\t\t\tmouse.click(ris.getCenter());\n" + "\t\t\treturn;\n"
				+ "\t\t}\n\n" + "\t\tScreenRegion ris = screen.find(element);\n" + "\t\tmouse.click(ris.getCenter());\n"
				+ "\t\tfor(int i=0; i<=v; i++) {\n" + "\t\t\tkeyboard.type(Key.DOWN);\n" + "\t\t}\n"
				+ "\t\tkeyboard.type(Key.ENTER);\n" + "\t\treturn"));
		return method;
	}

	/**
	 * Creates a stub method to manage Clear commands with the visual technology of
	 * Sikuli
	 * 
	 * @return
	 */
	private static MethodDeclaration createVisualMethodClear() {
		// create the method
		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "clear");
		// add a parameter to the method
		Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("Target", 0), "element");
		param.setVarArgs(false);
		ASTHelper.addParameter(method, param);

		// add a body to the method
		BlockStmt block = new BlockStmt();
		method.setBody(block);

		List<NameExpr> throws_ = new LinkedList<NameExpr>();
		throws_.add(new NameExpr("InterruptedException"));
		method.setThrows(throws_);

		ASTHelper.addStmt(block, new NameExpr("click(element)"));
		ASTHelper.addStmt(block, new NameExpr("int i = 100"));
		ASTHelper.addStmt(block, new NameExpr("while(i > 0) {"));
		ASTHelper.addStmt(block, new NameExpr("\tkeyboard.type(Key.BACKSPACE)"));
		ASTHelper.addStmt(block, new NameExpr("\ti--"));
		ASTHelper.addStmt(block, new NameExpr("}"));
		ASTHelper.addStmt(block, new NameExpr("//keyboard.type(Key.CMD + Key.ALT + Key.BACKSPACE)"));
		return method;
	}

	/**
	 * Creates a stub method to manage Type commands with the visual technology of
	 * Sikuli
	 * 
	 * @return
	 */
	private static MethodDeclaration createVisualMethodType() {
		// create the method
		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "type");
		// add a parameter to the method
		Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("Target", 0), "element");
		param.setVarArgs(false);
		Parameter param2 = ASTHelper.createParameter(ASTHelper.createReferenceType("String", 0), "value");
		param2.setVarArgs(false);
		ASTHelper.addParameter(method, param);
		ASTHelper.addParameter(method, param2);

		// add a body to the method
		BlockStmt block = new BlockStmt();
		method.setBody(block);

		List<NameExpr> throws_ = new LinkedList<NameExpr>();
		throws_.add(new NameExpr("InterruptedException"));
		method.setThrows(throws_);

		ASTHelper.addStmt(block, new NameExpr("click(element)"));
		ASTHelper.addStmt(block, new NameExpr("keyboard.type(value)"));
		return method;
	}

	/**
	 * Creates a stub method to manage Click commands with the visual technology of
	 * Sikuli
	 * 
	 * @return
	 */
	private static MethodDeclaration createVisualMethodClick() {

		// create the method
		MethodDeclaration method = new MethodDeclaration(ModifierSet.PUBLIC, ASTHelper.VOID_TYPE, "click");
		// add a parameter to the method
		Parameter param = ASTHelper.createParameter(ASTHelper.createReferenceType("Target", 0), "element");
		param.setVarArgs(false);
		ASTHelper.addParameter(method, param);

		// add a body to the method
		BlockStmt block = new BlockStmt();
		method.setBody(block);

		List<NameExpr> throws_ = new LinkedList<NameExpr>();
		throws_.add(new NameExpr("InterruptedException"));
		method.setThrows(throws_);

		ASTHelper.addStmt(block, new NameExpr("Thread.sleep(500);\n" + "\t\tscreen.setScore(1.00);\n\n"
				+ "\t\tif(screen.find(element) == null) { \n" + "\t\t\tmouse.click(screen.getCenter());\n"
				+ "\t\t\tkeyboard.type(Key.PAGE_DOWN);\n" + "\t\t\tThread.sleep(2000);\n \t\t}\n\n"
				+ "\t\tris = screen.find(element);\n" + "\t\twhile(ris == null){\n" + "\t\t\tmouse.wheel(1, 2);\n"
				+ "\t\t\tThread.sleep(500);\n" + "\t\t\tris = screen.find(element);\n \t\t}\n\n"
				+ "\t\tCanvas canvas = new DesktopCanvas();\n" + "\t\tcanvas.addBox(ris).display(1);\n"
				+ "\t\tmouse.click(ris.getCenter());\n" + "\t\tThread.sleep(1000)"));
		return method;
	}

	/**
	 * Removes useless Selenium WebDriver methods from the compilation unit
	 * 
	 * @return
	 */
	public static void removeUselessMethods(CompilationUnit cu) {
		List<TypeDeclaration> types = cu.getTypes();
		for (TypeDeclaration type : types) {
			List<BodyDeclaration> members = type.getMembers();
			List<BodyDeclaration> membersCleaned = new LinkedList<BodyDeclaration>();
			for (BodyDeclaration member : members) {
				if (member instanceof MethodDeclaration) {
					MethodDeclaration method = (MethodDeclaration) member;
					membersCleaned.add(method);
				} else if (member instanceof ConstructorDeclaration) {
					;
				} else {
					membersCleaned.add(member);
				}
			}
			type.setMembers(membersCleaned);
		}
	}

}
