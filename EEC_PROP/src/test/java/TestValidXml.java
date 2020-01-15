import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import ru.cbgr.EEC.HelperBase;
import ru.cbgr.EEC.XPathBaseHelper;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class TestValidXml {

  private HelperBase base = new HelperBase();
  private String pathToInitMessage = "";
  private String pathToLog = "";
  private int kolvoMSG ;


  /*Переменные для assert`ов*/
  private String conversationID = "";


  @BeforeSuite
  private void initial() {

    /*Переменные настройки подключения к шлюзу*/
    base.setHostName("eek-test1-ip-mq1.tengry.com");  //Адресс шлюза
    base.setChannel("ESB.SVRCONN");                   //Канал
    base.setPort(1414);                               //Порт
    base.setQueueManager("RU.IIS.QM");                //Менеджер очередей
    base.setQueueSending("GATEWAY.EXT.IN");           //Очередь для отправки сообщений
    base.setQueueRecieve("Q.ADDR5");                  //Тупиковая очередь для ответный сообщений


    /*Настройка переменных теста под определенное тестируемое ОП.
     * Названия должны совпадать с названиями папок где хранятся файлы для отправки.
     * Например :
     * \src\main\resources\OP_02\FLC\MSG.001_TRN.001\     - путь к файлам инициирующих сообщений
     * \src\main\resources\OP_02\FLC\MSG.001_TRN.001\Log     - путь к логам, туда сохранятся файлы отправленных и ответных сообщений*/

    base.setOpName("OP_02");                           //Название папки общего процесса
    base.setTipMSG("FLC");                             //Название папки с типом проверок (ошибки ФЛК или Valid)
    base.setTipTRN("MSG.001_TRN.001");                 //Название папки проверяемой транзакции
    base.setNumberMSG("MSG_001.xml");                  //Название инициирующего сообщения

    kolvoMSG = 2;                                  //Ожидаемое количество сообщений при проверке транзакции


    pathToInitMessage = base.getPathCommon() + base.getOpName() + "/" + base.getTipMSG() + "/" + base.getTipTRN() + "/" + base.getNumberMSG();
    pathToLog = base.getPathCommon() + base.getOpName() + "/" + base.getTipMSG() + "/" + base.getTipTRN() + "/" + "Log/";

  }

  @Test
  public void test() {

    try {

      /*Инициализация подключения и создания необходимых переменных*/
      base.init(base.getHostName(), base.getChannel(), base.getPort(), base.getQueueManager(), base.getQueueSending(), base.getQueueRecieve());

      /*Обнуляем очередь получения ответных сообщений*/
      base.clearQueue(base.getQueueReceiver());

      /*Создание сообщения на отправку*/
      String fileInit = base.filePreparation(pathToInitMessage);

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + base.randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Запись отправляемого MSG в файл*/
      base.writeMsgToHdd(fileInit, pathToLog + "Init_MSG_001.xml");

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      conversationID = base.variableFromXml(pathToLog + "Init_MSG_001.xml", "//int:ConversationID/text()");

      /*Отправка сообщения*/
      base.sendMsg(base.getQueueSession(), base.getQueueSender(), fileInit);

      /*Установка задержки для того чтобы ПРОП успел сформировать ответные сообщения и они попали в тупиковую очередь*/
      Thread.sleep(5000);//задержка на получение ответа от ПРОП

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder*/
      base.receiveMsgFromQueue(kolvoMSG, base.getQueueSession(), base.getQueueReciev(), pathToLog);

      /*Обнуляем очередь получения ответных сообщений*/
      base.clearQueue(base.getQueueReceiver());

      /*Запись полученных MSG в файл*/
      //writeMsgToHdd(stringBuilder.toString().replaceAll("UTF","utf"), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml");

      /*Остановка*/
      base.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testAssertFor_Msg_Prs() {
    if (new File(pathToLog + "Received_MSG_PRS.xml").exists()) {
      assertEquals(XPathBaseHelper.go(pathToLog + "Received_MSG_PRS.xml",
              "//int:ConversationID/text()"), conversationID);
      System.out.println("int:ConversationID - " + conversationID);

    }
  }

}