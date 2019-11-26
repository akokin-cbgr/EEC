import com.ibm.mq.jms.JMSC;
import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.*;

public class Main {

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
      TextMessage textMessage = queueSession.createTextMessage("put some message here");
      textMessage.setJMSReplyTo(queue);
      textMessage.setJMSType("mcd://xmlns");//message type
      //textMessage.setJMSExpiration(50*1000);//message expiration
      textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt

      /*Create sender queue */
      QueueSender queueSender = queueSession.createSender(queueSession.createQueue("Q.ADDR5"));
      //queueSender.setTimeToLive(50*1000);
      queueSender.send(textMessage);

      /*After sending a message we get message id */
      System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
      String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


      /*Within the session we have to create queue reciver */
      QueueReceiver queueReceiver = queueSession.createReceiver(queue);


      /*Receive the message from*/
      //Message message = queueReceiver.receive(2*1000);
      //String responseMsg = ((TextMessage) message).getText();
      //System.out.println(responseMsg);

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