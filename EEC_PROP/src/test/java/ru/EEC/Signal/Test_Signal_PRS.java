package ru.EEC.Signal;

import org.testng.annotations.Test;
import ru.EEC.TestBase;

import static org.testng.Assert.assertEquals;


public class Test_Signal_PRS extends TestBase {


  @Test()
  public void test_Signal_PRS() {

    setPathToInitMessage("OP_02/VALID/MSG.001_TRN.001/MSG_001.xml");
    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");

    assertEquals(testAssert_For_Signal("Received_MSG_PRS.xml"), "Passed");

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