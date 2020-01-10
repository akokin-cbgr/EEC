import javax.jms.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

public class HelperBase {
  /*Метод генерации UUID при помощи стандартной библиотеки из java.util*/
  static UUID uuid() {
    return UUID.randomUUID();
  }

  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
     Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  static int randInt(int min, int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt((max - min) + 1) + min;
    return randomNum;
  }

  /*Генерация строки определенной длинны из определенного набора символов
      diapazon - набор символов из которых будет генерироваться строка
      kol_vo - длинна генерируемой строки*/
  private static String randString(String diapazon, int kol_vo) {
    //проверки и вывод текста с ошибкой
    if (diapazon.equals("")) {
      System.out.println("ОШИБКА - Используемый диапазон значений не может быть пустым");
    }
    if (kol_vo < 1) {
      System.out.println("ОШИБКА - Количество символов в генерируемой строке не может быть отрицательным");
    }
    char[] chars = diapazon.toCharArray();//преобразуем строку в массив символов
    Random rand = new Random();//создаем объект типа Random
    StringBuilder stringBuilder = new StringBuilder();//создаем StringBuilder чтобы не плодить отдельные строки

    //пишем цикл длинной равной параметру kol_vo в котором выбираем случайный символ из массива и составляем новую строку
    for (int i = 1; i < kol_vo + 1; i++) {
      int randomNum = rand.nextInt(diapazon.length());//генерация случайного числа от 0 до величины длинны массива diapazon
      stringBuilder.append(chars[randomNum]);//собираем строку из выбранных символов в каждом цикле
    }
    return stringBuilder.toString();
  }

  static String getFile(String fileName) {
    StringBuilder result = new StringBuilder();
    WorkWithMQ workWithMQ = new WorkWithMQ();
    ClassLoader classLoader = workWithMQ.getClass().getClassLoader();
    File file = new File(Objects.requireNonNull(classLoader.getResource(fileName)).getFile());
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.toString();
  }

  /*Метод преобразования полученных файлов из очереди MQ в виде строки String*/
  static String onMessage(Message message) {
    try {

      if (message instanceof BytesMessage) {

        BytesMessage bytesMessage = (BytesMessage) message;
        byte[] data = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(data);
        bytesMessage.reset();
        return new String(data);
      } else if (message instanceof TextMessage) {

        TextMessage textMessage = (TextMessage) message;
        return textMessage.getText();
      }

    } catch (JMSException jmsEx) {
      jmsEx.printStackTrace();
    }
    return "";
  }

  /*Метод очистки очереди IBM MQ. В качестве параметра передается очередь получатель*/
  static void clearQueue(QueueReceiver queueReceiver) {
    try {
      while (true) {
        Message receive = queueReceiver.receiveNoWait();
        if (receive == null) break;
      }
    } catch (JMSException e) {
      e.printStackTrace();
    }
    /* РАБОЧИЙ ЧЕРНОВИК ЕЩЁ ОДНОЙ РЕАЛИЗАЦИИ ОЧИСТКИ ОЧЕРЕДИ
    try {
      QueueBrowser browser = queueSession.createBrowser(queueReciev);
      Enumeration e = browser.getEnumeration();
      while (e.hasMoreElements()) {
        Message message = (Message) e.nextElement();
        queueReceiver.receive();
        //Message message = queueReceiver.receive(100);// Обнуляем очередь от сообщений
      }
      browser.close();*/
  }
}
