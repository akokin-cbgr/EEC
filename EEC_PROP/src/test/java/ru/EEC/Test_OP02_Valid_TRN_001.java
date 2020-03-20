package ru.EEC;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

public class Test_OP02_Valid_TRN_001 extends TestBase {

  @BeforeClass
  private void set() {
    /*Настройка переменных теста под определенное тестируемое ОП.
     * Названия должны совпадать с названиями папок где хранятся файлы для отправки.
     * Например :
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\        - путь к файлам инициирующих сообщений
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\Log     - путь к логам, туда сохранятся файлы отправленных и ответных сообщений
     * В папке log создаются файлы с именами :
     * - Received_MSG_PRS.xml
     * - Received_MSG_RCV.xml
     * - Received_MSG_ERR.xml
     * - Received_MSG_002.xml
     * - Received_MSG_004.xml
     * - Received_MSG_XXX.xml
     * */
    setPathToInitMessage("OP_02/VALID/MSG.001_TRN.001/MSG_001.xml");
    setNameOfSaveInitMessage("Init_MSG_001.xml");

    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");

    /*Очистка папки с логами*/
    deleteAllFilesFolder(getPathToLog());

    /*Обнуляем очередь получения ответных сообщений*/
    clearQueue(getQueueReceiver());

  }

  @Test()
  public void test_TRN() {
    try {
      /*Создание сообщения на отправку*/
      assertTrue(checkInitFileExist());
      String fileInit = filePreparation(getPathToInitMessage());

      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      /*Запись отправляемого MSG в файл*/
      writeMsgToHdd(fileInit, getPathToLog() + "Init_MSG_001.xml");

      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
      setConversationID(variableFromXml(getPathToLog() + getNameOfSaveInitMessage(), "//int:ConversationID/text()"));

      /*Отправка сообщения*/
      sendMsg(getQueueSession(), getQueueSender(), fileInit);

      /*Шаг проверки очереди на наличие ответных сообщений от ПРОП с ограничением максимального времени ожидания*/
      checkAndWaitMsgInQueue(60);

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder
       * После этого проверка вернувшегося stringBuilder на null
       * Если будет null то тест упадет.
       * Внутри метода receiveMsgFromQueue реализовано условие возврата null если stringBuilder будет пустой по причине отсутствия сообщений в тупиковой очереди*/
      receiveMsgFromQueue();

      /*Проверки что созданы файлы ответных сообщений*/
      assertTrue(new File(getPathToLog() + "Received_MSG_PRS.xml").exists(),"Ответный файл Received_MSG_04.xml в папке " + getPathToLog() + " не создан!\n");
      assertTrue(new File(getPathToLog() + "Received_MSG_004.xml").exists(),"Ответный файл Received_MSG_04.xml в папке " + getPathToLog() + " не создан!\n");

      /*Вывод в консоль ID транзакции*/
      System.out.println(
              "ID транзакции                  - " + getConversationID() + "\n");


    } catch (Exception e) {
      e.printStackTrace();
    }
  }


  @Test(priority = 1, enabled = false)
  public void test_For_Msg_RCV() {
    assertEquals(testAssert_For_Signal("Received_MSG_RCV.xml"), "Passed");
  }


  @Test(priority = 2, enabled = false)
  public void test_For_Msg_PRS() {
    assertEquals(testAssert_For_Signal("Received_MSG_PRS.xml"), "Passed");
  }


  @Test(priority = 3, enabled = false)
  public void test_For_Msg_004() {
    assertEquals(testAssert_For_Reply_Msg("Received_MSG_004.xml",
            "csdo:ProcessingResultCode", "3",
            "csdo:DescriptionText", "Сведения добавлены"),
            "Passed");
  }

}