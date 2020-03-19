import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class Test_OP06_Valid_TRN_001 extends TestBase {

  /**/



  @BeforeSuite
  private void initial() {

    /*Переменные настройки подключения к шлюзу*/
    setHostName("eek-test1-ip-mq-sync.tengry.com");  //Адресс шлюза SYNC
    setChannel("ESB.SVRCONN");                   //Канал
    setPort(1414);                               //Порт
    setQueueManager("SYNC.IIS.QM");                //Менеджер очередей SYNC
    setQueueSending("ADP.PROP.IN");           //Очередь для отправки сообщений
    setQueueRecieve("Q.ADDR1");                  //Тупиковая очередь для ответных сообщений


    /*Настройка переменных теста под определенное тестируемое ОП.
     * Названия должны совпадать с названиями папок где хранятся файлы для отправки.
     * Например :
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\        - путь к файлам инициирующих сообщений
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\Log     - путь к логам, туда сохранятся файлы отправленных и ответных сообщений*/

//    setOpName("OP_06");                           //Название папки общего процесса
//    setTipMSG("VALID");                             //Название папки с типом проверок (ошибки ФЛК или Valid)
//    setTipTRN("MSG.001_TRN.001");                 //Название папки проверяемой транзакции
//    setNumberMSG("MSG_001.xml");                  //Название инициирующего сообщения

    /*Очистка папки с логами*/
    deleteAllFilesFolder(getPathToLog());
  }

  @Test()
  public void test_TRN() {

    try {
      setPathToInitMessage("OP_06\\VALID\\MSG.001_TRN.001\\MSG_001.xml");
      setPathToLog("OP_06\\VALID\\MSG.001_TRN.001\\Log");
      /*Инициализация подключения и создания необходимых переменных*/
      init(getHostName(), getChannel(), getPort(), getQueueManager(), getQueueSending(), getQueueRecieve());

      /*Обнуляем очередь получения ответных сообщений*/
      clearQueue(getQueueReceiver());

      /*Создание сообщения на отправку*/
      assertTrue(checkInitFileExist());
      String fileInit = filePreparation(getPathToInitMessage());

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:RegistrationNumberIdentifier>", ">" + randInt(1000, 9999) + "/" + randInt(100, 999) + "</casdo:RegistrationNumberIdentifier>");

      /*Запись отправляемого MSG в файл*/
      writeMsgToHdd(fileInit, getPathToLog() + "Init_MSG_001.xml");

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      setConversationID(variableFromXml(getPathToLog() + "Init_MSG_001.xml", "//int:ConversationID/text()"));

      /*Отправка сообщения*/
      sendMsg(getQueueSession(), getQueueSender(), fileInit);

      /*Шаг проверки очереди на наличие ответных сообщений от ПРОП с ограничением максимального времени ожидания*/
      checkAndWaitMsgInQueue(60);

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder
       * После этого проверка вернувшегося stringBuilder на null
       * Если будет null то тест упадет.
       * Внутри метода receiveMsgFromQueue реализовано условие возврата null если stringBuilder будет пустой по причине отсутствия сообщений в тупиковой очереди*/
      assertNotNull(receiveMsgFromQueue(getQueueSession(), getQueueReciev(), getPathToLog()));

      /*Проверки что созданы файлы ответных сообщений*/
      assertTrue(checkLogFileExist("Received_MSG_PRS.xml"));
      assertTrue(checkLogFileExist("Received_MSG_002.xml"));

      /*Вывод в консоль ID транзакции*/
      System.out.println(
              "ID транзакции                  - " + getConversationID() + "\n");

      /*Остановка*/
      close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Test(priority = 1, enabled = false)
  public void test_For_Msg_RCV() {
    assertEquals(testAssert_For_Signal("Received_MSG_RCV.xml"), "Passed");
  }


  @Test(priority = 2)
  public void test_For_Msg_PRS() {
    assertEquals(testAssert_For_Signal("Received_MSG_PRS.xml"), "Passed");
  }


  @Test(priority = 3)
  public void test_For_Msg_004() {
    assertEquals(testAssert_For_Reply_Msg("Received_MSG_002.xml",
            "csdo:ProcessingResultCode", "3",
            "csdo:DescriptionText", "Сведения добавлены"),
            "Passed");
  }

}