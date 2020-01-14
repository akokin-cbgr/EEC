import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import ru.cbgr.EEC.HelperBase;
import ru.cbgr.EEC.XPathBaseHelper;

import java.io.File;

import static org.testng.Assert.assertEquals;

public class TestValidXml extends HelperBase {
  /*Переменные для assert`ов*/
  private String conversationID = "";

  @BeforeSuite
  public void initial(){
    /*Переменные настройки подключения к шлюзу*/
    setHostName("eek-test1-ip-mq1.tengry.com");  //Адресс шлюза
    setChannel("ESB.SVRCONN");                   //Каналл
    setPort(1414);                               //Порт
    setQueueManager("RU.IIS.QM");                //Менеджер очередей
    setQueueSending("GATEWAY.EXT.IN");           //Очередь для отправки сообщений
    setQueueRecieve("Q.ADDR5");                  //Тупиковая очередь для ответный сообщений


    /*Настройка переменных теста под определенное тестируемое ОП.
     * Названия должны совпадать с названиями папок где хранятся файлы для отправки.
     * Например :
     * \src\main\resources\OP_02\FLC\MSG.001_TRN.001\     - путь к файлам инициирующих сообщений
     * \src\main\resources\OP_02\FLC\MSG.001_TRN.001\Log     - путь к логам, туда сохранятся файлы отправленных и ответных сообщений*/

    setOpName("OP_02");                           //Название папки общего процесса
    setTipMSG("FLC");                             //Название папки с типом проверок (ошибки ФЛК или Valid)
    setTipTRN("MSG.001_TRN.001");                 //Название папки проверяемой транзакции
    setNumberMSG("MSG_001.xml");                  //Название инициирующего сообщения

  }

  @Test
  public void test() {

    try {

      /*Инициализация подключения и создания необходимых переменных*/
      init(getHostName(), getChannel(), getPort(), getQueueManager(), getQueueSending(), getQueueRecieve());

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(getQueueReceiver());

      /*Создание сообщения на отправку*/
      String fileInit = filePreparation(getPathToInitMessage());

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Запись отправляемого MSG в файл*/
      writeMsgToHdd(fileInit, getPathToLog() + "Init_MSG_001.xml");

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      conversationID = variableFromXml(getPathToInitMessage(), "//int:ConversationID/text()");

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
    if (new File(getPathToLog() +"Received_MSG_PRS.xml").exists()) {
      assertEquals(XPathBaseHelper.go(getPathToLog() +"Received_MSG_PRS.xml",
              "//int:ConversationID/text()"), conversationID );
      System.out.println("int:ConversationID - " + conversationID);

    }
  }

}