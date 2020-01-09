import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.Queue;
import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;

public class WorkWithMQ {

  /*Метод генерации UUID при помощи стандартной библиотеки из java.util*/
  private static UUID uuid() {
    return UUID.randomUUID();
  }


  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
   Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  private static int randInt(int min, int max) {
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


  private static String getFile(String fileName) {
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
  private static void clearQueue(QueueReceiver queueReceiver) {
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


  public static void main(String[] args) {

    try {
      //устанавливаем параметры подключения
      MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
      mqQueueConnectionFactory.setHostName("eek-test1-ip-mq1.tengry.com");
      mqQueueConnectionFactory.setChannel("ESB.SVRCONN");
      mqQueueConnectionFactory.setPort(1414);
      mqQueueConnectionFactory.setQueueManager("RU.IIS.QM");
      mqQueueConnectionFactory.setTransportType(MQJMS_TP_CLIENT_MQ_TCPIP);

      QueueConnection queueConnection = mqQueueConnectionFactory.createQueueConnection("", "");//создаем соединение и запускаем сессию
      queueConnection.start();
      QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      /*Создаем очереди отправки и получения*/
      Queue queueSend = queueSession.createQueue("GATEWAY.EXT.IN");
      Queue queueReciev = queueSession.createQueue("Q.ADDR5");
      QueueSender queueSender = queueSession.createSender(queueSend);//указываем в какую очередь отправить сообщение
      QueueReceiver queueReceiver = queueSession.createReceiver(queueReciev);//указываем очередь откуда читать ответное сообщение


      /*ЧЕРНОВИК Create text message */
      /*File file = new File("resources/MSG_2.xml");//считываем из файла
      FileReader fileReader = new FileReader(file);//Создание объекта FileReader
      int b = (int) file.length();//вычисляем необходимую длинну массива исходя из длины XML
      char[] a = new char[b];//Создаем массив символов с вычисленной ранее длинной
      fileReader.read(a);//записываем в массив посимвольно весь файл
      String myStr = new String(a).trim();//преобразуем массив символов в строку*/
      //fileReader.close();


      //Обнуляем очередь и делаем задержку на получение ответов от ПРОП
      clearQueue(queueReceiver);



      /*Создание сообщения на отправку*/
      //String myStr = getFile("OP_02/FLC/MSG.001_TRN.001/FLC_01.xml");//считываем из файла XML
      String myStr = getFile("OP_02/FLC/MSG.001_TRN.001/MSG.001.xml");//считываем из файла XML
      myStr = myStr.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
      myStr = myStr.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
      myStr = myStr.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
      myStr = myStr.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");
      myStr = myStr.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      //Запись отправляемого MSG в файл для статистики
      File file_w1 = new File("D:\\Java_learn\\EEC\\EEC\\EEC_PROP\\src\\main\\resources\\OP_02\\FLC\\MSG.001_TRN.001\\Log\\MSG_01.xml");
      FileWriter writerInit = new FileWriter(file_w1);
      writerInit.write(myStr);
      writerInit.flush();
      writerInit.close();


      //передаем сообщение
      TextMessage textMessage = queueSession.createTextMessage(myStr);
      //в случае необходимости устанавливаем параметры для отправляемого сообщения
      //textMessage.setJMSReplyTo(queueReciever);
      //textMessage.setJMSType("mcd://xmlns");//message type
      //textMessage.setJMSExpiration(50*1000);//message expiration
      //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt


      //queueSender.setTimeToLive(50*1000);// установка времени жизни сообщения

      //отправляем в очередь ранее созданное сообщение
      queueSender.send(textMessage);
      System.out.println("Сообщение отправлено");




      /*Вариант получения JMSCorrelationID*/
      //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
      //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


      Thread.sleep(4000);//задержка на получение ответа от ПРОП


      //Создаем браузер для наблюдения за очередью
      QueueBrowser browser = queueSession.createBrowser(queueReciev);
      Enumeration e = browser.getEnumeration();
      StringBuilder stringBuilder = new StringBuilder();
      while (e.hasMoreElements()) {
               /* Проба
               BytesMessage message = (BytesMessage) e.nextElement();
                byte[] byteData = new byte[(int) message.getBodyLength()];
                message.readBytes(byteData);
                message.reset();
                String stringMessage = new String(byteData);
                System.out.println("Browse [" + stringMessage + "]");
                System.out.println("Done");*/

        //Получение сообщений
        Message message = (Message) e.nextElement();
        stringBuilder.append(onMessage(message)).append("\n");
        queueReceiver.receive();
        //String responseMsg = ((TextMessage) message).getText();
      }


      //System.out.println("Сообщение получено \n " + test);
      //boolean bool = test.toString().contains("<sgn:Description>Ошибка контроля</sgn:Description>");
      //System.out.println("Проверка - " + bool);
      //Обнуляем очередь
      clearQueue(queueReceiver);
      System.out.println("Сообщение получено" + stringBuilder);
      File file_w = new File("D:\\Java_learn\\EEC\\EEC\\EEC_PROP\\src\\main\\resources\\OP_02\\FLC\\MSG.001_TRN.001\\Log\\01.xml");
      FileWriter writerResponse = new FileWriter(file_w);
      writerResponse.write(stringBuilder.toString());
      writerResponse.flush();
      writerResponse.close();

      browser.close();
      queueSender.close();
      queueReceiver.close();
      queueSession.close();
      queueConnection.close();


    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}