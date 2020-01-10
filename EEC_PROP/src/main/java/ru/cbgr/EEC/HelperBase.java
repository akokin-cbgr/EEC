package ru.cbgr.EEC;

import javax.jms.Queue;
import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HelperBase {
  /*Метод генерации UUID при помощи стандартной библиотеки из java.util*/
  public static UUID uuid() {
    return UUID.randomUUID();
  }

  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
     Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  public static int randInt(int min, int max) {
    Random rand = new Random();
    int randomNum = rand.nextInt((max - min) + 1) + min;
    return randomNum;
  }

  /*Генерация строки определенной длинны из определенного набора символов
      diapazon - набор символов из которых будет генерироваться строка
      kol_vo - длинна генерируемой строки*/
  public static String randString(String diapazon, int kol_vo) {
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

  public static String getFile(String fileName) {
    StringBuilder result = new StringBuilder();
    HelperBase helperBase = new HelperBase();
    ClassLoader classLoader = helperBase.getClass().getClassLoader();
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
  public static String onMessage(Message message) {
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
  public static void clearQueue(QueueReceiver queueReceiver) {
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

  public static void writeSendingMsgToHdd(String fileInit, String filePath) throws IOException {
    //Запись отправляемого MSG в файл для статистики
    File file = new File(filePath);
    FileWriter writerInit = new FileWriter(file);
    writerInit.write(fileInit);
    writerInit.flush();
    writerInit.close();
  }

  public static void sendMsg(QueueSession queueSession, QueueSender queueSender, String fileInit) throws JMSException {
    TextMessage textMessage = queueSession.createTextMessage(fileInit);
    //в случае необходимости устанавливаем параметры для отправляемого сообщения
    //textMessage.setJMSReplyTo(queueReciever);
    //textMessage.setJMSType("mcd://xmlns");//message type
    //textMessage.setJMSExpiration(50*1000);//message expiration
    //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt
    //queueSender.setTimeToLive(50*1000);// установка времени жизни сообщения
    queueSender.send(textMessage);//отправляем в очередь ранее созданное сообщение
    System.out.println("Сообщение отправлено");
  }

  /*Вариант получения JMSCorrelationID*/
  //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
  //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


  public static String filePreparation(String filePath) {
    String fileRaw = getFile(filePath);//считываем из файла XML
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
    fileRaw = fileRaw.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");
    return fileRaw;
  }

  public static StringBuilder receiveMsgFromQueue(QueueSession queueSession, Queue queueReciev) throws JMSException, IOException {
    //Создаем браузер для наблюдения за очередью
    QueueBrowser browser = queueSession.createBrowser(queueReciev);
    Enumeration e = browser.getEnumeration();
    StringBuilder stringBuilder = new StringBuilder();
    int i = 0;
    while (e.hasMoreElements()) {
      //Получение сообщений
      Message message = (Message) e.nextElement();
      stringBuilder.append(onMessage(message)).append("\n");
      writeSendingMsgToHdd(stringBuilder.toString().replaceAll("UTF","utf"), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/" + i + ".xml");
      i++;
      stringBuilder.delete(0,stringBuilder.length());
      //queueReceiver.receive();
      //String responseMsg = ((TextMessage) message).getText();
    }

    System.out.println("Сообщение получено");
    browser.close();
    return stringBuilder;
  }

}
