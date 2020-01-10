import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.testng.annotations.Test;
import ru.cbgr.EEC.HelperBase;
import ru.cbgr.EEC.XPathBaseHelper;

import javax.jms.*;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;
import static org.testng.Assert.assertEquals;

public class TestValidXml extends HelperBase {


  @Test
  public void test() {

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

      /*Обнуляем очередь получения ответных сообщений*/
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
      Thread.sleep(5000);//задержка на получение ответа от ПРОП

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder*/
      //StringBuilder stringBuilder = receiveMsgFromQueue(queueSession, queueReciev);
      receiveMsgFromQueue(queueSession, queueReciev);

      //System.out.println("Сообщение получено \n " + test);
      //boolean bool = test.toString().contains("<sgn:Description>Ошибка контроля</sgn:Description>");
      //System.out.println("Проверка - " + bool);

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(queueReceiver);

      /*Запись полученных MSG в файл*/
      //writeSendingMsgToHdd(stringBuilder.toString().replaceAll("UTF","utf"), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml");

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
  public void test2() {
    assertEquals(XPathBaseHelper.go("src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml",
            "//csdo:EDocId/text()"),
            "5b6d4d94-bda7-4d1d-8a23-1bb8b2bb5629");
    System.out.println(XPathBaseHelper.go("src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml",
            "//csdo:EDocId/text()"));

  }

}