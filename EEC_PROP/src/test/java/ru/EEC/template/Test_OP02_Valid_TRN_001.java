package ru.EEC.template;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import ru.EEC.TestBase;
import ru.cbgr.EEC.XPathBaseHelper;

import java.io.File;

import static org.testng.Assert.assertTrue;

public class Test_OP02_Valid_TRN_001 extends TestBase {

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
    setPathToInitMessage("OP_02/VALID/MSG.001_TRN.001/MSG_001.xml");
    setPathToLog("OP_02/VALID/MSG.001_TRN.001/Log/");

    setNameOfSaveInitMessage("Init_MSG_001.xml");

    /*Очистка папки с логами*/
    deleteAllFilesFolder(getPathToLog());


  }

  /*Для настройки теста стандартной TRN.001(создание записи в БД ПРОП) под другой ОП необходимо:
   * 1. Прописать путь к инициирующему файлу и логам.
   * 2. Изменить генерацию уникального бизнес ключа в XML, чтобы не упала ФЛК на стороне ПРОП
   * */

  @Test()
  public void test_TRN() {
    try {
      /*Создание сообщения на отправку*/
      String fileInit = filePreparation(getPathToInitMessage());


      /*ГЕНЕРАЦИЯ УНИКАЛЬНОГО КЛЮЧА ДЛЯ ДАННОГО ОП, В ОТПРАВЛЯЕМОЙ XML*/
      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");


      /*Запись отправляемого MSG в файл*/
      writeMsgToHdd(fileInit, getPathToLog() + getNameOfSaveInitMessage());

      /*Отправка сообщения*/
      sendMsg(fileInit);

      /*Шаг проверки очереди на наличие ответных сообщений от ПРОП с ограничением максимального времени ожидания*/
      checkAndWaitMsgInQueue(60);

      /*Вычитка ответных сообщений из очереди queueReciev и передача их в stringBuilder
       * Внутри метода receiveMsgFromQueue реализована проверка assertTrue если stringBuilder будет пустой по причине отсутствия сообщений в тупиковой очереди*/
      receiveMsgFromQueue();

      /*Проверки теста, о том что ответные файлы по транзакции успешно созданы. Это означает что они были действительно вычитаны методом receiveMsgFromQueue()*/
      assertTrue(new File(getPathToLog() + "Received_MSG_PRS.xml").exists(),
              "Ответный файл Received_MSG_PRS.xml в папке " + getPathToLog() + " не создан!\n");
      assertTrue(new File(getPathToLog() + "Received_MSG_004.xml").exists(),
              "Ответный файл Received_MSG_004.xml в папке " + getPathToLog() + " не создан!\n");

      /*Вывод в консоль ID транзакции*/
      System.out.println(
              "ID транзакции                  - " + XPathBaseHelper.go(getPathToLog() + getNameOfSaveInitMessage(), "//int:ConversationID/text()") + "\n");


    } catch (Exception e) {
      e.printStackTrace();
    }
  }


}


//       МУСОР и заготовки

//      /*Передаем в приватное поле сгенерированный conversationID для последующего использования в тесте с полученными ответными сообщениями*/
//      setConversationID(variableFromXml(getPathToLog() + getNameOfSaveInitMessage(), "//int:ConversationID/text()"));//старая рализация
//      assertTrue(new File(getPathToLog() + getNameOfSaveInitMessage()).exists(),
//              "\nОШИБКА ТЕСТА - В папке \n" + getPathToLog() + "\n" +
//              "отсутствует инициирующий файл для транзакции - " + getNameOfSaveInitMessage() + "\n");
//      setConversationID(XPathBaseHelper.go(getPathToLog() + getNameOfSaveInitMessage(), "//int:ConversationID/text()"));

