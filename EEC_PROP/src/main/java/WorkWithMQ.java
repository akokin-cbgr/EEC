import com.ibm.mq.jms.MQQueueConnectionFactory;

import javax.jms.Queue;
import javax.jms.*;
import java.util.*;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;

public class WorkWithMQ extends HelperBase {


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


      //Обнуляем очередь получения ответных сообщений
      clearQueue(queueReceiver);



      /*Создание сообщения на отправку*/
      //String myStr = getFile("OP_02/FLC/MSG.001_TRN.001/FLC_01.xml");//считываем из файла XML
      String fileInit = filePreparation("OP_02/FLC/MSG.001_TRN.001/MSG.001.xml");

      /*Генерация уникального ключа для данного ОП*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Запись отправляемого MSG в файл*/
      writeSendingMsgToHdd(fileInit, "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/MSG_01.xml");

      /*Отправка сообщения*/
      sendMsg(queueSession, queueSender, fileInit);

      /*Установка задержки для того чтобы ПРОП успел сформировать ответные сообщения и они попали в тупиковую очередь*/
      Thread.sleep(4000);//задержка на получение ответа от ПРОП


      //Создаем браузер для наблюдения за очередью
      QueueBrowser browser = queueSession.createBrowser(queueReciev);
      Enumeration e = browser.getEnumeration();
      StringBuilder stringBuilder = new StringBuilder();
      while (e.hasMoreElements()) {
        //Получение сообщений
        Message message = (Message) e.nextElement();
        stringBuilder.append(onMessage(message)).append("\n");
        //queueReceiver.receive();
        //String responseMsg = ((TextMessage) message).getText();
      }


      //System.out.println("Сообщение получено \n " + test);
      //boolean bool = test.toString().contains("<sgn:Description>Ошибка контроля</sgn:Description>");
      //System.out.println("Проверка - " + bool);
      //Обнуляем очередь
      clearQueue(queueReceiver);
      System.out.println("Сообщение получено" + stringBuilder);
      writeSendingMsgToHdd(stringBuilder.toString(), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml");

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