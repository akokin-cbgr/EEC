package ru.EEC.P_CC_01_TRN_001.Test_Signal;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.EEC.TestBase;


public class Test_Signal_PRS extends TestBase {

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
    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");
    setNameOfSaveInitMessage("Init_MSG_001.xml");
  }


  @Test()
  public void test_Signal_PRS() {

    setPathToInitMessage("OP_02/VALID/MSG.001_TRN.001/MSG_001.xml");
    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");

    testAssert_For_Signal("Received_MSG_PRS.xml");

  }
}