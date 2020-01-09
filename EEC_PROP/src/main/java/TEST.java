import java.util.Random;

public class TEST {

  private static String randString(int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt(10);
    int randomNum2 = rand.nextInt(max);
    String ret = "" + randomNum + "   " + randomNum2;
    return ret;
  }


  private static String randString2(String diapazon, int kol_vo) {
    char[] chars = diapazon.toCharArray();
    Random rand = new Random();
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 1; i < kol_vo + 1; i++) {
      int randomNum = rand.nextInt(diapazon.length());
      stringBuilder.append(String.valueOf(chars[randomNum]));
    }
    return stringBuilder.toString();
  }

  public static void main(String[] args) {
    System.out.println(randString(100) + "\n");
    System.out.println(randString2("qwerty12345",15));

  }
}
