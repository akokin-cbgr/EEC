import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;

public class WorkWithMQ {

  private static UUID uuid() {
    return UUID.randomUUID();
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


  private static void clearQueue(QueueReceiver queueReceiver) {
    try {
      queueReceiver.receiveNoWait();// Обнуляем очередь от сообщений
    } catch (JMSException e) {
      e.printStackTrace();
    }
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


      /*Create text message */
      /*File file = new File("resources/MSG_2.xml");//считываем из файла
      FileReader fileReader = new FileReader(file);//Создание объекта FileReader
      int b = (int) file.length();//вычисляем необходимую длинну массива исходя из длины XML
      char[] a = new char[b];//Создаем массив символов с вычисленной ранее длинной
      fileReader.read(a);//записываем в массив посимвольно весь файл
      String myStr = new String(a).trim();//преобразуем массив символов в строку*/
      //fileReader.close();

      /*Создание сообщения на отправку*/
      String myStr = getFile("OP_02/FLC/MSG.001_TRN.001/FLC_01.xml");//считываем из файла
      myStr = myStr.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
      myStr = myStr.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
      myStr = myStr.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
      myStr = myStr.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");

      //передаем в сессию наше сообщение
      TextMessage textMessage = queueSession.createTextMessage(myStr);
      //в случае необходимости устанавливаем параметры сообщения
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


      //Создаем браузер для наблюдения за очередью
      Thread.sleep(3000);
      QueueBrowser browser = queueSession.createBrowser(queueReciev);
      Enumeration e = browser.getEnumeration();
      StringBuilder test = new StringBuilder();
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
        test.append(onMessage(message)).append("\n");
        //String responseMsg = ((TextMessage) message).getText();

        System.out.println("Сообщение получено \n " + test);
        boolean bool = test.toString().contains("<sgn:Description>Ошибка контроля</sgn:Description>");
        System.out.println("Проверка - " + bool);

        //Обнуляем очередь
        clearQueue(queueReceiver);
      }

      System.out.println("Сообщение получено");
      File file_w = new File("D:\\Java_learn\\EEC\\EEC\\EEC_PROP\\src\\main\\resources\\OP_02\\FLC\\MSG.001_TRN.001\\Log\\01.xml");
      FileWriter writer = new FileWriter(file_w);
      writer.write(test.toString());
      writer.flush();
      writer.close();

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