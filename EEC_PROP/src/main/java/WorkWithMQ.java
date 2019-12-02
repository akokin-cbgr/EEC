import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class WorkWithMQ {

  public static UUID uuid() {
    UUID uuid = UUID.randomUUID();
    return uuid;
  }

  private String getFile(String fileName) {
    StringBuilder result = new StringBuilder("");
    //Get file from resources folder
    ClassLoader classLoader = getClass().getClassLoader();
    File file = new File(classLoader.getResource(fileName).getFile());
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.append(line).append("\n");
      }
      scanner.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.toString();
  }


  public static void main(String[] args) {
    WorkWithMQ workWithMQ = new WorkWithMQ();

    try {

      MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
      mqQueueConnectionFactory.setHostName("eek-test1-ip-mq1.tengry.com");
      mqQueueConnectionFactory.setChannel("ESB.SVRCONN");
      mqQueueConnectionFactory.setPort(1414);
      mqQueueConnectionFactory.setQueueManager("RU.IIS.QM");
      mqQueueConnectionFactory.setTransportType(JMSC.MQJMS_TP_CLIENT_MQ_TCPIP);


      QueueConnection queueConnection = mqQueueConnectionFactory.createQueueConnection("", "");
      queueConnection.start();
      QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

      /*Create response queue */
      Queue queue = queueSession.createQueue("Q.ADDR5");



      /*Create text message */
      /*File file = new File("resources/MSG_2.xml");//считываем из файла
      FileReader fileReader = new FileReader(file);//Создание объекта FileReader
      int b = (int) file.length();//вычисляем необходимую длинну массива исходя из длины XML
      char[] a = new char[b];//Создаем массив символов с вычисленной ранее длинной
      fileReader.read(a);//записываем в массив посимвольно весь файл
      String myStr = new String(a).trim();//преобразуем массив символов в строку*/

      //fileReader.close();

      String myStr = workWithMQ.getFile("OP_02/FLC/MSG.001_TRN.001/FLC_01.xml");//считываем из файла
      myStr = myStr.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
      myStr = myStr.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
      myStr = myStr.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
      myStr = myStr.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");
      //System.out.println(myStr);
      TextMessage textMessage = queueSession.createTextMessage(myStr);//передаем в сессию наше сообщение
      //textMessage.setJMSReplyTo(queue);
      //textMessage.setJMSType("mcd://xmlns");//message type
      //textMessage.setJMSExpiration(50*1000);//message expiration
      //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt

      /*Create sender queue */

      QueueSender queueSender = queueSession.createSender(queueSession.createQueue("GATEWAY.EXT.IN"));//указываем в какую очередь отправить сообщение
      //queueSender.setTimeToLive(50*1000);//установка времени жизни сообщения
      queueSender.send(textMessage);//отправляем в очередь ранее созданное сообщение
      System.out.println("Сообщение отправлено");

      /*After sending a message we get message id */
      //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
      //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


      /*Within the session we have to create queue reciver */
      QueueReceiver queueReceiver = queueSession.createReceiver(queue);//указываем очередь откуда читать ответное сообщение




      /*Receive the message from*/
      Message message = queueReceiver.receive(3000);
      BytesMessage byteMessage = (BytesMessage) message;
      byte[] byteData = null;
      byteData = new byte[(int) byteMessage.getBodyLength()];
      byteMessage.readBytes(byteData);
      byteMessage.reset();
      String stringMessage = new String(byteData);

      //String responseMsg = ((TextMessage) message).getText();

      Boolean bool = stringMessage.contains("<sgn:Description>Ошибка контроля</sgn:Description>");
      System.out.println("Сообщение получено \n " + stringMessage);
      System.out.println("Проверка - " + bool);


      File file_w = new File("D:\\Java_learn\\EEC\\EEC\\EEC_PROP\\src\\main\\resources\\OP_02\\FLC\\MSG.001_TRN.001\\Log\\01.xml");
      FileWriter writer = new FileWriter(file_w);
      writer.write(stringMessage);
      writer.flush();
      writer.close();


      queueSender.close();
      queueReceiver.close();
      queueSession.close();
      queueConnection.close();


    } catch (JMSException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}