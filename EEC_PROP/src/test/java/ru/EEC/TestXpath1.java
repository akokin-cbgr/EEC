import org.testng.annotations.Test;
import ru.cbgr.EEC.XPathBaseHelper;

import static org.testng.Assert.assertEquals;

public class TestXpath1 extends XPathBaseHelper {

  @Test
  public void test() {
    assertEquals(go("src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml",
            "//csdo:EDocId/text()"),
            "5b6d4d94-bda7-4d1d-8a23-1bb8b2bb5629");

  }
}