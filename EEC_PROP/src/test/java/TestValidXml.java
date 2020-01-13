import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.testng.annotations.Test;
import ru.cbgr.EEC.HelperBase;
import ru.cbgr.EEC.XPathBaseHelper;

import javax.jms.*;
import java.io.File;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;
import static org.testng.Assert.assertEquals;

public class TestValidXml extends HelperBase {
  /*Переменные для assert`ов*/
  private String conversationID = "";

  /*Переменные настройки подключения к шлюзу*/
  private String hostName = "eek-test1-ip-mq1.tengry.com";
  private String channel = "ESB.SVRCONN";
  private int port = 1414;
  private String queueManager = "RU.IIS.QM";
  private String queueSending = "GATEWAY.EXT.IN";
  private String queueRecieve = "Q.ADDR5";

  /*Переменные с путями к файлам*/
  private String pathCommon = "src/main/resources/";
  private String pathToInitMessage = pathCommon + "OP_02/FLC/MSG.001_TRN.001/MSG.001.xml";
  private String pathToLogForInitXML = pathCommon + "OP_02/FLC/MSG.001_TRN.001/Log/Init_MSG_001.xml";


  @Test
  public void test() {

    try {
      //устанавливаем параметры подключения
      MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
      mqQueueConnectionFactory.setHostName(hostName);
      mqQueueConnectionFactory.setChannel(channel);
      mqQueueConnectionFactory.setPort(port);
      mqQueueConnectionFactory.setQueueManager(queueManager);
      mqQueueConnectionFactory.setTransportType(MQJMS_TP_CLIENT_MQ_TCPIP);
      QueueConnection queueConnection = mqQueueConnectionFactory.createQueueConnection("", "");//создаем соединение и запускаем сессию
      queueConnection.start();
      QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      /*Создаем очереди отправки и получения*/
      Queue queueSend = queueSession.createQueue(queueSending);
      Queue queueReciev = queueSession.createQueue(queueRecieve);
      QueueSender queueSender = queueSession.createSender(queueSend);//указываем в какую очередь отправить сообщение
      QueueReceiver queueReceiver = queueSession.createReceiver(queueReciev);//указываем очередь откуда читать ответное сообщение

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(queueReceiver);

      /*Создание сообщения на отправку*/
      String fileInit = filePreparation(pathToInitMessage);

      /*Генерация уникального ключа для данного ОП*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Запись отправляемого MSG в файл*/
      writeMsgToHdd(fileInit, pathToLogForInitXML);

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      conversationID = variableFromXml(pathToLogForInitXML, "//int:conversationID/text()");

      /*Отправка сообщения*/
      sendMsg(queueSession, queueSender, fileInit);

      /*Установка задержки для того чтобы ПРОП успел сформировать ответные сообщения и они попали в тупиковую очередь*/
      Thread.sleep(5000);//задержка на получение ответа от ПРОП

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder*/
      receiveMsgFromQueue(queueSession, queueReciev);

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(queueReceiver);

      /*Запись полученных MSG в файл*/
      //writeMsgToHdd(stringBuilder.toString().replaceAll("UTF","utf"), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml");

      /*Остановка*/
      queueSender.close();
      queueReceiver.close();
      queueSession.close();
      queueConnection.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testAssertFor_Msg_Prs() {
    if (new File(pathCommon + "OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_PRS.xml").exists()) {
      assertEquals(XPathBaseHelper.go(pathCommon + "OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_PRS.xml",
              "//int:conversationID/text()"), conversationID
      );
      System.out.println("int:conversationID - " + conversationID);

    }
  }

}