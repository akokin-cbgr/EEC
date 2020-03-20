import com.jayway.restassured.RestAssured;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestApi {
  @Test
  public void test() {
    String response = RestAssured.get("http://eek-testcdb-services1.tengry.com:8093/public/api/registry/P.CC.01/check-points/registryUpdateTime").getBody().asString();
    JSONObject jsonObject = new JSONObject(response);
    Assert.assertTrue(jsonObject.get("updateDateTime").toString().contains("2019"));
  }

}