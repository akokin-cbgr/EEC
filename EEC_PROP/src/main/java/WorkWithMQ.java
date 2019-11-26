import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileReader;

public class WorkWithMQ {

  public static void main(String[] args) {
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
      File file = new File("D://MSG_2.xml");//считываем из файла
      FileReader fileReader = new FileReader(file);//Создание объекта FileReader
      int b = (int)file.length();//вычисляем необходимую длинну массива исходя из длины XML
      char[] a=new char[b];//Создаем массив символов с вычисленной ранее длинной
      fileReader.read(a);//записываем в массив посимвольно весь файл
      String myStr = new String(a).trim();//преобразуем массив символов в строку
      TextMessage textMessage = queueSession.createTextMessage(myStr);//передаем в сессию наше сообщение
      //textMessage.setJMSReplyTo(queue);
      //textMessage.setJMSType("mcd://xmlns");//message type
      //textMessage.setJMSExpiration(50*1000);//message expiration
      //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt

      /*Create sender queue */

      //QueueSender queueSender = queueSession.createSender(queueSession.createQueue("Q.ADDR5"));//указываем в какую очередь отправить сообщение
      QueueSender queueSender = queueSession.createSender(queueSession.createQueue("GATEWAY.EXT.IN"));//указываем в какую очередь отправить сообщение
      //queueSender.setTimeToLive(50*1000);//установка времени жизни сообщения
      queueSender.send(textMessage);//отправляем в очередь ранее созданное сообщение
      System.out.println("Сообщение отправлено");

      /*After sending a message we get message id */
      //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
      //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


      /*Within the session we have to create queue reciver */
      //QueueReceiver queueReceiver = queueSession.createReceiver(queue);//указываем очередь откуда читать ответное сообщение


      /*Receive the message from*/
     // Message message = queueReceiver.receive(10*1000);
      //String responseMsg = ((TextMessage) message).getText();
      //System.out.println(responseMsg);

      queueSender.close();
      //queueReceiver.close();
      queueSession.close();
      queueConnection.close();
      fileReader.close();


    } catch (JMSException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}