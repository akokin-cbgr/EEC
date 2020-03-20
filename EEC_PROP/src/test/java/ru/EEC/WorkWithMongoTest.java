import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.cbgr.EEC.WorkWithMongo;

import java.util.NoSuchElementException;

@Test
public class WorkWithMongoTest extends WorkWithMongo {
  public void test() throws NoSuchElementException {
  try {
    JSONObject jsonObject = new JSONObject(request("eek-test1prop-mg2.tengry.com:27017", "service-prop-65", "radioElectronicDeviceRegistryDetailsType",
          "propositionInclusionRadioElectronicDeviceId","PBY00000000000000204"));
  Assert.assertTrue(jsonObject.get("statusCode").toString().contains("002"));
  }
  catch (NoSuchElementException e){
    System.out.println(e);
  }
  }
}
