package ru.cbgr.EEC;

import org.w3c.dom.Document;

import javax.jms.Queue;
import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class HelperBase {
  /*Метод генерации UUID при помощи стандартной библиотеки из java.util*/
  private static UUID uuid() {
    return UUID.randomUUID();
  }

  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
     Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  protected static int randInt(int min, int max) {
    Random rand = new Random();
    return rand.nextInt((max - min) + 1) + min;
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

  static String getFile(String fileName) {
    StringBuilder result = new StringBuilder();
    File file = new File(fileName);
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
  private static String onMessage(Message message) {
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
  protected static void clearQueue(QueueReceiver queueReceiver) {
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

  protected static void writeSendingMsgToHdd(String fileInit, String filePath) throws IOException {
    //Запись отправляемого MSG в файл для статистики
    File file = new File(filePath);
    FileWriter writerInit = new FileWriter(file);
    writerInit.write(fileInit);
    writerInit.flush();
    writerInit.close();
  }

  protected static void sendMsg(QueueSession queueSession, QueueSender queueSender, String fileInit) throws JMSException {
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах полученных сообщений
    TextMessage textMessage = queueSession.createTextMessage(fileInit);

    /*в случае необходимости устанавливаем параметры для отправляемого сообщения*/
    //textMessage.setJMSReplyTo(queueReciever);
    //textMessage.setJMSType("mcd://xmlns");//message type
    //textMessage.setJMSExpiration(50*1000);//message expiration
    //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt
    //queueSender.setTimeToLive(50*1000);// установка времени жизни сообщения

    if (textMessage.getText().contains("MSG.001")) {
      result.append("MSG.001");
    } else if (textMessage.getText().contains("MSG.002")) {
      result.append("MSG.002");
    } else if (textMessage.getText().contains("MSG.003")) {
      result.append("MSG.003");
    } else if (textMessage.getText().contains("MSG.004")) {
      result.append("MSG.004");
    } else if (textMessage.getText().contains("MSG.005")) {
      result.append("MSG.005");
    } else if (textMessage.getText().contains("MSG.006")) {
      result.append("MSG.006");
    }
    queueSender.send(textMessage);//отправляем в очередь ранее созданное сообщение
    System.out.println("Сообщение " + result + " отправлено.");
  }

  /*Вариант получения JMSCorrelationID*/
  //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
  //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


  protected static String filePreparation(String filePath) {
    String fileRaw = getFile(filePath);//считываем из файла XML
    /*Производим замену UUID на сгенерированные*/
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
    fileRaw = fileRaw.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");
    return fileRaw;
  }

  protected static String variableFromXml(String filepath, String xpath) {
    return XPathBaseHelper.go(filepath, xpath);
  }

  /*Метод форматирования XML в читаемый вид*/
  private static String formatXml(String unFormatedXml) throws IOException {
    Document document = XmlStringFormatter.convertStringToDocument(unFormatedXml);
    return XmlStringFormatter.toPrettyXmlString(document);
  }

  protected static StringBuilder receiveMsgFromQueue(QueueSession queueSession, Queue queueReciev) throws JMSException, IOException {
    QueueBrowser browser = queueSession.createBrowser(queueReciev);//Создаем браузер для наблюдения за очередью
    Enumeration e = browser.getEnumeration();//получаем Enumeration
    StringBuilder stringBuilder = new StringBuilder();//создаем stringBuilder для записи в него сообщения из очереди
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах полученных сообщений
    /*Цикл вычитки и последующей записи в соответствующие файлы полученных в очереди сообщений*/
    while (e.hasMoreElements()) {
      Message message = (Message) e.nextElement(); //Получение сообщения
      stringBuilder.append(onMessage(message)).append("\n"); // запись в stringBuilder вычитанного сообщения
      /*Условия сортировки сообщений по типу*/
      if (stringBuilder.toString().contains("P.MSG.PRS")) {
        writeSendingMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_PRS.xml");
        result.append("- MSG.PRS\n");
      } else if (stringBuilder.toString().contains("P.MSG.ERR")) {
        writeSendingMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_ERR.xml");
        result.append("- MSG.ERR\n");
      } else if (stringBuilder.toString().contains("P.CC.01.MSG.004")) {
        writeSendingMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_004.xml");
        result.append("- MSG.004\n");
      } else {
        writeSendingMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_XXX.xml");
        result.append("- MSG.XXX\n");
      }
      stringBuilder.delete(0, stringBuilder.length());
      //queueReceiver.receive();
      //String responseMsg = ((TextMessage) message).getText();
    }

    System.out.println("Получено: \n" + result); // формирование строки-отчета в консоли
    browser.close();
    return stringBuilder;
  }

}
