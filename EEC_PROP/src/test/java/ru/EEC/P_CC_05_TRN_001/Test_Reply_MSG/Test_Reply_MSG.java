package ru.EEC.P_CC_05_TRN_001.Test_Reply_MSG;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.EEC.TestBase;


public class Test_Reply_MSG extends TestBase {



  @BeforeClass
  private void configTest() {
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
    setPathToLog("OP_06/VALID/MSG.001_TRN.001/Log/");
    setNameOfSaveInitMessage("Init_MSG_001.xml");
  }


  @Test()
  public void Test_Reply_MSG() {

    testAssert_For_Reply_Msg("Received_MSG_002.xml",
            "csdo:ProcessingResultCode","3",
            "csdo:DescriptionText","Сведения добавлены");

//    assertEquals(testAssert_For_Signal("Received_MSG_RCV.xml"), "Passed");

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