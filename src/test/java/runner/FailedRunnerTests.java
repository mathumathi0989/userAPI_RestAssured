package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(features = {"@target/failedrerun.txt"}, glue = {"stepdefinition","hooks"},
plugin = { "pretty", "html:target/cucumber-reports/restassured-api-Failedtestreport.html",
		"io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
		"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:" ,"rerun:target/failedrerun.txt"})
public class FailedRunnerTests extends AbstractTestNGCucumberTests{

	
	
}
