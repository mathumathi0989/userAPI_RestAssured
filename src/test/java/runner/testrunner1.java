package runner;
 

import org.testng.annotations.Test;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
 
@Test
@CucumberOptions(features="src/test/resources/features",
glue= {"stepdefinition","hooks"},
plugin={"pretty",
		"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:",
		"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
		"html:target/cucumber-reports/restassured-api-testreport.html"}
		)

public class testrunner1 extends AbstractTestNGCucumberTests {
	 
	
	
	
	
}
