import java.util.Random;

public class TEST {

  private static String randString1(int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt(10);
    int randomNum2 = rand.nextInt(max);
    String ret = "" + randomNum + "   " + randomNum2;
    return ret;
  }


  private static String randString(String diapazon, int kol_vo) {
    if (diapazon.equals("")) {
      System.out.println("ОШИБКА - Используемый диапазон значений не может быть пустым");
    }
    if (kol_vo < 1) {
      System.out.println("ОШИБКА - Количество символов в генерируемой строке не может быть отрицательным");
    }
    char[] chars = diapazon.toCharArray();
    Random rand = new Random();
    StringBuilder stringBuilder = new StringBuilder();
    for (int i = 1; i < kol_vo + 1; i++) {
      int randomNum = rand.nextInt(diapazon.length());
      stringBuilder.append(chars[randomNum]);
    }
    return stringBuilder.toString()
            /*+ "\nДлинна - " + stringBuilder.length()*/;
  }


  public static void main(String[] args) {
    //System.out.println(randString1(100) + "\n");
    System.out.println(randString("123456789", 4));

  }
}
