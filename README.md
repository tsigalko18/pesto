# PESTO

PESTO (Page Object Transformation Tool) is a tool 
to transform a DOM-based web test suite, created using [Selenium WebDriver](http://www.seleniumhq.org/projects/webdriver/), into a visual web test suite based on the [Sikuli](http://sikulix.com) image recognition capabilities. The web test suite must adopt the [Page Object](https://github.com/SeleniumHQ/selenium/wiki/PageObjects) and [Page Factory](https://github.com/SeleniumHQ/selenium/wiki/PageFactory) design patterns.

###  Build

You can import the project within the Eclipse IDE, or build it from command line by typing

`mvn compile`

###  Run toy example

1. parameters setting, through the `Settings` class in the package `pesto`. This serves to specify where the reference test suites are. It is suggested to put the Selenium page objects and tests under the `src/main/resources/` directory. No further edit should be required in this file for the correct execution of the toy example.
2. DOM-based test suite execution, located in `src/main/resources/testSuite`. Right-click on `DemoSeleniumTestSuite` -> Run As -> JUnit Test.
A `TestSuiteRunner` class is also available in the package `pesto`. The `classRunner` should be edited to point to a JUnit Test Suite class. More info [here](https://github.com/junit-team/junit4/wiki/aggregating-tests-in-suites). The project shall create an `output` folder with the visual locators for each test, under the `screenshots` folder. See an example [here](https://github.com/tsigalko18/pesto/tree/master/output/screenshots).
3.  page object trasformation, through the `PageObjectTransformer` class in the package `pesto`. The class will tranform the Selenium page objects in `src/main/resources/poSelenium` to Sikuli. The output will be saved in the `src/main/resources/poSikuli` package.  See an example [here](https://github.com/tsigalko18/pesto/tree/master/src/main/resources/poSikuli).
4. test case trasformation, through the `TestTransformer` class in the package `pesto`. The class will tranform the tests in `src/main/resources/testSuite` to Sikuli. The output will be still saved in the `src/main/resources/testSuite` package. (The three phases can be run at once using the `PestoMain` class in the package `pesto`)

###  Publications

The tool and its empirical evaluation have been part in the following res.

- Maurizio Leotta, Andrea Stocco, Filippo Ricca, Paolo Tonella. **Automated Generation of Visual Web Tests from DOM-based Web Tests.** _Proceedings of 30th ACM/SIGAPP Symposium on Applied Computing (SAC 2015)_, 13-17 April, 2015, Salamanca, Spain, pp.775-782, ACM, 2015. [DOI](10.1145/2695664.2695847) bib()

- Andrea Stocco, Maurizio Leotta, Filippo Ricca, Paolo Tonella.
**PESTO: A Tool for Migrating DOM-based to Visual Web Tests.** _Proceedings of 14th IEEE International Working Conference on Source Code Analysis and Manipulation (SCAM 2014)_, 28-29 September 2014, Victoria, British Columbia, Canada, pp.65-70, IEEE, 2014. [DOI](10.1109/SCAM.2014.36)

---
bibliography: mybib.bib
nocite: '@*'
...

# Bibliography