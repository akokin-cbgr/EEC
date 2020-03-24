package ru.EEC.Signal;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.EEC.TestBase;


public class Test_Signal_RCV extends TestBase {

  @BeforeClass
  private void set() {
    /*Настройка переменных теста под определенное тестируемое ОП.
     * Указываем путь к каталогу с инициирующим файлом и путь куда будет сохранено вычитанное сообщение
     *
     * Названия должны совпадать с названиями папок где хранятся файлы для отправки.
     * Образец :
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\        - путь к файлам инициирующих сообщений
     * \src\main\resources\OP_02\VALID\MSG.001_TRN.001\Log     - путь к логам, туда сохранятся файлы отправленных и ответных сообщений
     *
     * В папке Log создаются файлы с именами :
     * - Received_MSG_PRS.xml
     * - Received_MSG_RCV.xml
     * - Received_MSG_ERR.xml
     * - Received_MSG_002.xml
     * - Received_MSG_004.xml
     * - Received_MSG_XXX.xml
     * */
    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");
    setNameOfSaveInitMessage("Init_MSG_001.xml");
  }


  @Test()
  public void test_Signal_RCV() {

    setPathToInitMessage("OP_02/VALID/MSG.001_TRN.001/MSG_001.xml");
    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");

    testAssert_For_Signal("Received_MSG_RCV.xml");

//      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
//      setConversationID(variableFromXml(getPathToLog() + "Init_MSG_001.xml", "//int:ConversationID/text()"));

//      /*Проверки что созданы файлы ответных сообщений*/
//      assertTrue(checkLogFileExist("Received_MSG_PRS.xml"));
//      assertTrue(checkLogFileExist("Received_MSG_004.xml"));
//
//      /*Вывод в консоль ID транзакции*/
//      System.out.println(
//              "ID транзакции                  - " + getConversationID() + "\n");
//
//      /*Остановка*/
//      close();

  }
}