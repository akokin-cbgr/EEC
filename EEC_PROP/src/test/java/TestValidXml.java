import org.testng.annotations.Test;
import ru.cbgr.EEC.HelperBase;
import ru.cbgr.EEC.XPathBaseHelper;

import java.io.File;

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


  @Test
  public void test() {

    try {

      /*Инициализация подключения и создания необходимых переменных*/
      init(hostName, channel, port, queueManager, queueSending, queueRecieve);

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(getQueueReceiver());

      /*Создание сообщения на отправку*/
      String fileInit = filePreparation(pathToInitMessage);

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Запись отправляемого MSG в файл*/
      writeMsgToHdd(fileInit, pathToLogForInitXML);

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      conversationID = variableFromXml("src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/Init_MSG_001.xml", "//int:ConversationID/text()");

      /*Отправка сообщения*/
      sendMsg(getQueueSession(), getQueueSender(), fileInit);

      /*Установка задержки для того чтобы ПРОП успел сформировать ответные сообщения и они попали в тупиковую очередь*/
      Thread.sleep(5000);//задержка на получение ответа от ПРОП

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder*/
      receiveMsgFromQueue(getQueueSession(), getQueueReciev());

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(getQueueReceiver());

      /*Запись полученных MSG в файл*/
      //writeMsgToHdd(stringBuilder.toString().replaceAll("UTF","utf"), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml");

      /*Остановка*/
      close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testAssertFor_Msg_Prs() {
    if (new File(getPathCommon() + "OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_PRS.xml").exists()) {
      assertEquals(XPathBaseHelper.go(getPathCommon() + "OP_02/FLC/MSG.001_TRN.001/Log/Received_MSG_PRS.xml",
              "//int:ConversationID/text()"), conversationID );
      System.out.println("int:ConversationID - " + conversationID);

    }
  }

}