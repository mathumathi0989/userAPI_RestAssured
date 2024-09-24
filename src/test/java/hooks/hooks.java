package hooks;

import io.cucumber.java.Before;
import static io.restassured.RestAssured.*;

public class hooks {

	@Before
    public void setup() {
        // Base URL setup
        baseURI = System.getProperty("baseUrl", "https://userserviceapp-f5a54828541b.herokuapp.com/uap");
   
	
	}
	
	
}
