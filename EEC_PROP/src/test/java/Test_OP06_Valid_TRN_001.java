import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Test_OP06_Valid_TRN_001 {

  /**/
  private TestBase base = new TestBase();


  @BeforeSuite
  private void initial() {

    /*Переменные настройки подключения к шлюзу*/
    base.setHostName("eek-test1-ip-mq-sync.tengry.com");  //Адресс шлюза SYNC
    base.setChannel("ESB.SVRCONN");                   //Канал
    base.setPort(1414);                               //Порт
    base.setQueueManager("SYNC.IIS.QM");                //Менеджер очередей SYNC
    base.setQueueSending("ADP.PROP.IN");           //Очередь для отправки сообщений
    base.setQueueRecieve("Q.ADDR1");                  //Тупиковая очередь для ответных сообщений


    /*Настройка переменных теста под определенное тестируемое ОП.
     * Названия должны совпадать с названиями папок где хранятся файлы для отправки.
     * Например :
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\        - путь к файлам инициирующих сообщений
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\Log     - путь к логам, туда сохранятся файлы отправленных и ответных сообщений*/

    base.setOpName("OP_06");                           //Название папки общего процесса
    base.setTipMSG("VALID");                             //Название папки с типом проверок (ошибки ФЛК или Valid)
    base.setTipTRN("MSG.001_TRN.001");                 //Название папки проверяемой транзакции
    base.setNumberMSG("MSG_001.xml");                  //Название инициирующего сообщения

    /*Очистка папки с логами*/
    base.deleteAllFilesFolder(base.getPathToLog());
  }

  @Test()
  public void test_TRN() {

    try {

      /*Инициализация подключения и создания необходимых переменных*/
      base.init(base.getHostName(), base.getChannel(), base.getPort(), base.getQueueManager(), base.getQueueSending(), base.getQueueRecieve());

      /*Обнуляем очередь получения ответных сообщений*/
      base.clearQueue(base.getQueueReceiver());

      /*Создание сообщения на отправку*/
      assertTrue(base.checkInitFileExist());
      String fileInit = base.filePreparation(base.getPathToInitMessage());

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:RegistrationNumberIdentifier>", ">" + base.randInt(1000, 9999) + "/" + base.randInt(100, 999) + "</casdo:RegistrationNumberIdentifier>");

      /*Запись отправляемого MSG в файл*/
      base.writeMsgToHdd(fileInit, base.getPathToLog() + "Init_MSG_001.xml");

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      base.setConversationID(base.variableFromXml(base.getPathToLog() + "Init_MSG_001.xml", "//int:ConversationID/text()"));

      /*Отправка сообщения*/
      base.sendMsg(base.getQueueSession(), base.getQueueSender(), fileInit);

      /*Шаг проверки очереди на наличие ответных сообщений от ПРОП с ограничением максимального времени ожидания*/
      base.checkAndWaitMsgInQueue(60);

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder
       * После этого проверка вернувшегося stringBuilder на null
       * Если будет null то тест упадет.
       * Внутри метода receiveMsgFromQueue реализовано условие возврата null если stringBuilder будет пустой по причине отсутствия сообщений в тупиковой очереди*/
      assertNotNull(base.receiveMsgFromQueue(base.getQueueSession(), base.getQueueReciev(), base.getPathToLog()));

      /*Проверки что созданы файлы ответных сообщений*/
      assertTrue(base.checkLogFileExist("Received_MSG_PRS.xml"));
      assertTrue(base.checkLogFileExist("Received_MSG_002.xml"));

      /*Вывод в консоль ID транзакции*/
      System.out.println(
              "ID транзакции                  - " + base.getConversationID() + "\n");

      /*Остановка*/
      base.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Test(priority = 1, enabled = false)
  public void test_For_Msg_RCV() {
    assertEquals(base.testAssert_For_Signal("Received_MSG_RCV.xml"), "Passed");
  }


  @Test(priority = 2)
  public void test_For_Msg_PRS() {
    assertEquals(base.testAssert_For_Signal("Received_MSG_PRS.xml"), "Passed");
  }


  @Test(priority = 3)
  public void test_For_Msg_004() {
    assertEquals(base.testAssert_For_Reply_Msg("Received_MSG_002.xml",
            "csdo:ProcessingResultCode", "3",
            "csdo:DescriptionText", "Сведения добавлены"),
            "Passed");
  }

}