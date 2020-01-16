import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import ru.cbgr.EEC.XPathBaseHelper;

import java.io.File;
import java.util.Objects;

import static org.testng.Assert.*;

public class Test_OP02_Valid_TRN_001 {

  /**/
  private TestBase base = new TestBase();


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
      String fileInit = base.filePreparation(base.getPathToInitMessage());
      if (fileInit.equals("File not found!")) {
        System.out.println("Файл инициирующего сообщения не найден. \n" +
                "Путь - " + base.getPathToInitMessage());
        fail();
      }

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + base.randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Очистка папки с логами от предыдущих файлов*/
      base.deleteAllFilesFolder(base.getPathToLog());

      /*Запись отправляемого MSG в файл*/
      base.writeMsgToHdd(fileInit, base.getPathToLog() + "Init_MSG_001.xml");

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      base.setConversationID(base.variableFromXml(base.getPathToLog() + "Init_MSG_001.xml", "//int:ConversationID/text()"));

      /*Отправка сообщения*/
      base.sendMsg(base.getQueueSession(), base.getQueueSender(), fileInit);

      /*Установка задержки для того чтобы ПРОП успел сформировать ответные сообщения и они попали в тупиковую очередь*/
      Thread.sleep(10000);//задержка на получение ответа от ПРОП

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder
       * После этого проверка вернувшегося stringBuilder на null
       * Если будет null то тест упадет.
       * Внутри метода receiveMsgFromQueue реализовано условие возврата null если stringBuilder будет пустой по причине отсутствия сообщений в тупиковой очереди*/
      assertNotNull(base.receiveMsgFromQueue(2, base.getQueueSession(), base.getQueueReciev(), base.getPathToLog()));

      /**/
      System.out.println(
              "ID транзакции                  - " + base.getConversationID() + "\n");

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


  @Test(priority = 1, enabled = false)
  public void testAssert_For_Msg_RCV() {
    if (new File(base.getPathToLog() + "Received_MSG_RCV.xml").exists()) {
      if (Objects.requireNonNull(XPathBaseHelper.go(base.getPathToLog() + "Received_MSG_RCV.xml",
              "//int:ConversationID/text()")).equals(base.getConversationID())) {
        System.out.println("Тесты для - Received_MSG_RCV.xml:\n" +
                "int:ConversationID             - совпадает с ID транзакции\n");
      } else {
        System.out.println("Тесты для - Received_MSG_RCV.xml:\n" +
                "ОШИБКА ТЕСТА                   - int:ConversationID не совпадает с ID транзакции\n");
        fail();
      }
    } else {
      System.out.println("Тесты для - Received_MSG_RCV.xml:\n" +
              "ОШИБКА ТЕСТА - В папке \n" + base.getPathToLog() + "\n" +
              "отсутствует файл - Received_MSG_RCV.xml\n");
      fail();
    }
  }


  @Test(priority = 2)
  public void testAssert_For_Msg_PRS() {
    if (new File(base.getPathToLog() + "Received_MSG_PRS.xml").exists()) {
      if (Objects.requireNonNull(XPathBaseHelper.go(base.getPathToLog() + "Received_MSG_PRS.xml",
              "//int:ConversationID/text()")).equals(base.getConversationID())) {
        System.out.println("Тесты для - Received_MSG_PRS.xml:\n" +
                "int:ConversationID             - совпадает с ID транзакции\n");
      } else {
        System.out.println("Тесты для - Received_MSG_PRS.xml:\n" +
                "ОШИБКА ТЕСТА                   - int:ConversationID не совпадает с ID транзакции\n");
        fail();
      }
    } else {
      System.out.println("Тесты для - Received_MSG_PRS.xml:\n" +
              "ОШИБКА ТЕСТА - В папке \n" + base.getPathToLog() + "\n" +
              "отсутствует файл - Received_MSG_PRS.xml\n");
      fail();
    }
  }


  @Test(priority = 3)
  public void testAssert_For_Msg_004() {
    if (new File(base.getPathToLog() + "Received_MSG_004.xml").exists()) {
      base.setConversationID(base.variableFromXml(base.getPathToLog() + "Init_MSG_001.xml", "//int:ConversationID/text()"));
      if (Objects.requireNonNull(XPathBaseHelper.go(base.getPathToLog() + "Received_MSG_004.xml",
              "//int:ConversationID/text()")).equals(base.getConversationID())) {
        System.out.println("Тесты для - Received_MSG_004.xml:\n" +
                "int:ConversationID             - совпадает с ID транзакции");
      } else {
        System.out.println("Тесты для - Received_MSG_004.xml:\n" +
                "ОШИБКА ТЕСТА                   - int:ConversationID не совпадает с ID транзакции");
        fail();
      }
      if (Objects.requireNonNull(XPathBaseHelper.go(base.getPathToLog() + "Received_MSG_004.xml",
              "//csdo:ProcessingResultCode/text()")).equals("3")) {
        System.out.println(
                "csdo:ProcessingResultCode      - содержит верный код \"3\"");
      } else {
        System.out.println(
                "ОШИБКА ТЕСТА                   - csdo:ProcessingResultCode содержит НЕверный код");
        fail();
      }
      if (Objects.requireNonNull(XPathBaseHelper.go(base.getPathToLog() + "Received_MSG_004.xml",
              "//csdo:DescriptionText/text()")).equals("Сведения добавлены")) {
        System.out.println(
                "csdo:DescriptionText           - соответствует значению \"Сведения добавлены\"");
      } else {
        System.out.println(
                "ОШИБКА ТЕСТА                   - csdo:DescriptionText НЕ соответствует значению");
        fail();
      }
    } else {
      System.out.println("Тесты для - Received_MSG_004.xml:\n" +
              "ОШИБКА ТЕСТА - В папке \n" + base.getPathToLog() + "\n" +
              "отсутствует файл - Received_MSG_004.xml\n");
      fail();
    }
  }

}