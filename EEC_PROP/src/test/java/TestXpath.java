import org.testng.Assert;
import org.testng.annotations.Test;

public class TestXpath extends XPathBaseHelper {

  @Test
  public void test() {
    Assert.assertEquals(go("D:\\Java_learn\\EEC\\EEC\\EEC_PROP\\src\\main\\resources\\OP_02\\FLC\\MSG.001_TRN.001\\Log\\01.xml",
            "//sgn:EDocId/text()"), "1e152dd1-6d55-48dc-92c0-919fbab7a4f1");
    System.out.println(go("D:\\Java_learn\\EEC\\EEC\\EEC_PROP\\src\\main\\resources\\OP_02\\FLC\\MSG.001_TRN.001\\Log\\01.xml",
            "//sgn:EDocId/text()"));


  }
}