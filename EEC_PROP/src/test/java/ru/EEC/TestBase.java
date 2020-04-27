package ru.EEC;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.w3c.dom.Document;
import ru.cbgr.EEC.XPathBaseHelper;
import ru.cbgr.EEC.XmlStringFormatter;

import javax.jms.Queue;
import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static ru.EEC.Property.*;

abstract public class TestBase {

  private String nameOfSavedInitMessage;
  private String pathToInitMessage;
  private String pathToLog;

  /*Общие поля после инициализации*/
  private Queue queueReciev;

  private QueueSender queueSender;
  private QueueReceiver queueReceiver;
  private QueueSession queueSession;
  private QueueConnection queueConnection;

  /*Переменные для assert`ов*/
  private String conversationID;

  /*Последовательность действий выполняемых ДО теста*/
  @BeforeTest
  public void setUp() throws JMSException {
    /*Инициализация подключения и создания необходимых переменных*/
    //устанавливаем параметры подключения
    MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
    mqQueueConnectionFactory.setHostName(HOSTNAME);
    mqQueueConnectionFactory.setChannel(CHANNEL);
    mqQueueConnectionFactory.setPort(PORT);
    mqQueueConnectionFactory.setQueueManager(QUEUEMANAGER);
    mqQueueConnectionFactory.setTransportType(MQJMS_TP_CLIENT_MQ_TCPIP);
    //создаем соединение и запускаем сессию
    queueConnection = mqQueueConnectionFactory.createQueueConnection("", "");
    queueConnection.start();
    queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    /*Создаем очереди отправки и получения*/
    Queue queueSend = queueSession.createQueue(QUEUESENDING);
    queueReciev = queueSession.createQueue(QUEUERECIVE);
    //указываем в какую очередь отправить сообщение
    queueSender = queueSession.createSender(queueSend);
    //указываем очередь откуда читать ответное сообщение
    queueReceiver = queueSession.createReceiver(queueReciev);

    /*Обнуляем очередь получения ответных сообщений ДО выполнения теста*/
    clearQueue(queueReceiver);
  }


  /*Последовательность действий выполняемых ПОСЛЕ теста*/
  @AfterTest
  void close() throws JMSException {

    /*Обнуляем очередь получения ответных сообщений ПОСЛЕ выполнения теста*/
    clearQueue(queueReceiver);

    /*Остановка*/
    queueSender.close();
    queueReceiver.close();
    queueSession.close();
    queueConnection.close();
  }


  /*Метод генерации UUID при помощи стандартной библиотеки из java.util,
   * возвращается строка для использования в подготовке отправляемого инициирующего файла транзакции*/
  private String uuid() {
    return UUID.randomUUID().toString();
  }


  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
   * Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  protected int randInt(int min, int max) {
    //Random rand = new Random();
    //return rand.nextInt((max - min) + 1) + min;
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }


  /*Генерация строки определенной длинны из определенного набора символов
   * diapazon - набор символов из которых будет генерироваться строка
   * kol_vo - длинна генерируемой строки */
  public String randString(String diapazon, int kol_vo) {
    //проверки и вывод текста с ошибкой
    if (diapazon.equals("")) {
      System.out.println("ОШИБКА - Используемый диапазон значений не может быть пустым");
    }
    if (kol_vo < 1) {
      System.out.println("ОШИБКА - Количество символов в генерируемой строке не может быть отрицательным");
    }
    char[] chars = diapazon.toCharArray();//преобразуем строку в массив символов
    Random rand = new Random();//создаем объект типа Random
    StringBuilder stringBuilder = new StringBuilder();//создаем StringBuilder чтобы не плодить отдельные строки

    //пишем цикл длинной равной параметру kol_vo в котором выбираем случайный символ из массива и составляем новую строку
    for (int i = 1; i < kol_vo + 1; i++) {
      int randomNum = rand.nextInt(diapazon.length());//генерация случайного числа от 0 до величины длинны массива diapazon
      stringBuilder.append(chars[randomNum]);//собираем строку из выбранных символов в каждом цикле
    }
    return stringBuilder.toString();
  }


  /*Метод считывания файла с диска HDD в строку*/
  private String getFile(String fileName) {
    StringBuilder result = new StringBuilder();
    File file = new File(fileName);
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        result.append(line).append("\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result.toString();
  }


  /*Метод преобразования полученных файлов из очереди MQ в виде строки String*/
  private String onMessage(Message message) {
    try {
      if (message instanceof BytesMessage) {
        BytesMessage bytesMessage = (BytesMessage) message;
        byte[] data = new byte[(int) bytesMessage.getBodyLength()];
        bytesMessage.readBytes(data);
        bytesMessage.reset();
        return new String(data);
      } else if (message instanceof TextMessage) {
        TextMessage textMessage = (TextMessage) message;
        return textMessage.getText();
      }
    } catch (JMSException jmsEx) {
      jmsEx.printStackTrace();
    }
    return "";
  }


  /*Метод очистки очереди IBM MQ. В качестве параметра передается очередь получатель*/
  private void clearQueue(QueueReceiver queueReceiver) {
    try {
      while (true) {
        Message receive = queueReceiver.receiveNoWait();
        if (receive == null) break;
      }
    } catch (JMSException e) {
      e.printStackTrace();
    }
    /* РАБОЧИЙ ЧЕРНОВИК ЕЩЁ ОДНОЙ РЕАЛИЗАЦИИ ОЧИСТКИ ОЧЕРЕДИ
    try {
      QueueBrowser browser = queueSession.createBrowser(queueReciev);
      Enumeration e = browser.getEnumeration();
      while (e.hasMoreElements()) {
        Message message = (Message) e.nextElement();
        queueReceiver.receive();// Обнуляем очередь от сообщений
        //Message message = queueReceiver.receive(100);
      }
      browser.close();*/
  }


  /*Метод удаления всех файлов из указываемой папки*/
  protected void deleteAllFilesFolder(String path) {
    for (File myFile : Objects.requireNonNull(new File(path).listFiles()))
      if (myFile.isFile()) {
        myFile.delete();
      }
  }


  /*Метод записи передаваемого или получаемого сообщения на HDD*/
  protected void writeMsgToHdd(String fileInit, String filePath) throws IOException {
    File file = new File(filePath);
    FileWriter writerInit = new FileWriter(file);
    writerInit.write(fileInit);
    writerInit.flush();
    writerInit.close();
  }


  /*Метод отправки инициализирубщего сообщения в соответствующую очередь MQ*/
  protected void sendMsg(String fileInit) throws JMSException {
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах отправляемых сообщений
    TextMessage textMessage = queueSession.createTextMessage(fileInit);

    /*в случае необходимости устанавливаем параметры для отправляемого сообщения*/
    //textMessage.setJMSReplyTo(queueReciever);
    //textMessage.setJMSType("mcd://xmlns");                          //тип сообщения
    //textMessage.setJMSExpiration(50*1000);                          //время истечения
    //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);        //режим доставки сообщений постоянный или непостоянный
    //queueSender.setTimeToLive(50*1000);                             // установка времени жизни сообщения

    /*Определение типа отправляемого сообщения*/
    if (textMessage.getText().contains("MSG.001")) {
      result.append("MSG.001");
    } else if (textMessage.getText().contains("MSG.002")) {
      result.append("MSG.002");
    } else if (textMessage.getText().contains("MSG.003")) {
      result.append("MSG.003");
    } else if (textMessage.getText().contains("MSG.004")) {
      result.append("MSG.004");
    } else if (textMessage.getText().contains("MSG.005")) {
      result.append("MSG.005");
    } else if (textMessage.getText().contains("MSG.006")) {
      result.append("MSG.006");
    }
    queueSender.send(textMessage);//отправляем в очередь ранее созданное сообщение
    System.out.println("Запуск теста для ОП " + getPathToInitMessage().substring(22, 24) +
            " - TRN." + getPathToInitMessage().substring(43, 46) +
            "\nСообщение " + result + " отправлено:\n" +
            "- Очередь           - " + QUEUESENDING +
            "\n- Адрес шлюза       - " + HOSTNAME + "\n");
  }


  /*Вариант получения JMSCorrelationID*/
  //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
  //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


  /*Метод подготовки XML к отправке в очередь IBM MQ, идет генерация UUID в хедере отправляемого сообщения*/
  protected String filePreparation(String filePath) {
    /*Проверка что инициирующее сообщение существует в папке отправки*/
    assertTrue(new File(pathToInitMessage).exists(),
            "ОШИБКА ТЕСТА - Не найден инициирующий файл по пути: \n" + pathToInitMessage + "\n");
    /*Считываем из файла XML и производим замену UUID на сгенерированные*/
    String fileRaw = getFile(filePath)
            .replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid() + "</wsa:MessageID>")
            .replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid() + "</int:ConversationID>")
            .replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid() + "</int:ProcedureID>")
            .replaceAll(">.*</csdo:EDocId>", ">" + uuid() + "</csdo:EDocId>");
    return fileRaw;
  }

  /*Метод выборки значения из указанного файла XML согласно выражению xpath*/
  private String variableFromXml(String filepath, String xpath) {
    return XPathBaseHelper.go(filepath, xpath);
  }

  /*Метод форматирования XML в читаемый вид*/
  private String formatXml(String unFormatedXml) {
    Document document = XmlStringFormatter.convertStringToDocument(unFormatedXml);
    return XmlStringFormatter.toPrettyXmlString(document);
  }

  /*Метод ожидания сообщений из целевой очереди*/
  protected void checkAndWaitMsgInQueue(int maxWaitTimeSec) throws JMSException, InterruptedException {
    QueueBrowser browser = queueSession.createBrowser(queueReciev);//Создаем браузер для наблюдения за очередью
    Enumeration e = browser.getEnumeration();//получаем Enumeration
    int i = 0;
    while (!e.hasMoreElements()) {
      Thread.sleep(1000);
      i++;
      if (i == maxWaitTimeSec) {
        System.out.println("ОШИБКА ТЕСТА - Превышено время(" + maxWaitTimeSec + "секунд) ожидания ответов от ПРОП");
        break;
      }
    }
  }


  /*Метод получения сообщения из определенной очереди MQ*/

  protected void receiveMsgFromQueue() throws JMSException, IOException {
    QueueBrowser browser = queueSession.createBrowser(queueReciev);//Создаем браузер для наблюдения за очередью
    Enumeration e = browser.getEnumeration();//получаем Enumeration
    StringBuilder stringBuilder = new StringBuilder();//создаем stringBuilder для записи в него сообщения из очереди
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах полученных сообщений
    /*Цикл вычитки и последующей записи в соответствующие файлы полученных в очереди сообщений*/
    while (e.hasMoreElements()) {
      Message message = (Message) e.nextElement(); //Получение сообщения
      stringBuilder.append(onMessage(message)).append("\n"); // запись в stringBuilder вычитанного сообщения
      Message receive = queueReceiver.receiveNoWait(); // вычитываем сообщение из очереди для его удаления
      receive.clearBody();
      /*Условия сортировки сообщений по типу*/
      if (stringBuilder.toString().contains("P.MSG.PRS</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_PRS.xml");        //осуществляем запись на диск вычитанного сообщения из очереди
        result.append("- MSG.PRS\n");                              //формирование строки консоли о типе полученного сообщения
      } else if (stringBuilder.toString().contains("P.MSG.RCV</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_RCV.xml");        //осуществляем запись на диск вычитанного сообщения из очереди
        result.append("- MSG.RCV\n");                              //формирование строки консоли о типе полученного сообщения
      } else if (stringBuilder.toString().contains("P.MSG.ERR</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_ERR.xml");        //осуществляем запись на диск вычитанного сообщения из очереди
        result.append("- MSG.ERR\n");                              //формирование строки консоли о типе полученного сообщения
      } else if (stringBuilder.toString().contains("MSG.002</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_002.xml");        //осуществляем запись на диск вычитанного сообщения из очереди
        result.append("- MSG.002\n");                              //формирование строки консоли о типе полученного сообщения
      } else if (stringBuilder.toString().contains("MSG.004</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_004.xml");        //осуществляем запись на диск вычитанного сообщения из очереди
        result.append("- MSG.004\n");                              //формирование строки консоли о типе полученного сообщения
      } else {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_XXX.xml");        //осуществляем запись на диск вычитанного НЕИЗВЕСТНОГО сообщения из очереди
        result.append("- MSG.XXX\n");                              //формирование строки консоли о НЕИЗВЕСТНОМ типе полученного сообщения
      }
      /*Очистка stringBuilder*/
      stringBuilder.delete(0, stringBuilder.length());
    }

//    assertTrue(!(e.hasMoreElements()) ,
//            "\nИТОГ\n" + "ОШИБКА ТЕСТА - Очередь " + queueRecieve + " не содержит сообщений."); // формирование строки-отчета в консоли
    //queueReceiver.receive();
    //String responseMsg = ((TextMessage) message).getText();
    System.out.println("\nИТОГ\n" +
            "Получено и созданно в " + pathToLog + " : \n" + result); // формирование строки-отчета в консоли
    browser.close();
  }

  private void setConversationIdForAssert() {
    assertTrue(new File(pathToLog + nameOfSavedInitMessage).exists(), "\nОШИБКА ТЕСТА - В папке \n" + pathToLog + "\n" +
            "отсутствует файл - " + nameOfSavedInitMessage + "\n");
    conversationID = variableFromXml(pathToLog + nameOfSavedInitMessage, "//int:ConversationID/text()");
  }


  protected void testAssert_For_Reply_Msg(String checkedFile, String tegCheckCode, String resultCode, String tegDescriptionText, String descriptionText) {
    /*Проверка что проверяемый файл присутствует в папке*/
    assertTrue(new File(pathToLog + checkedFile).exists(),
            "\nТесты для - " + checkedFile + ":\n" +
                    "ОШИБКА ТЕСТА - В папке \n" + pathToLog + "\n" +
                    "отсутствует файл - " + checkedFile + "\n");

    /*Считывание ConversationID из сохраненного в Log после отправки инициирующего файла*/
    setConversationIdForAssert();

    /*Проверка сопадения ConversationID между ответным сообщением и инициирующим из папки Log*/
    assertEquals(conversationID, Objects.requireNonNull(XPathBaseHelper.go(pathToLog + checkedFile,
            "//int:ConversationID/text()")),
            "\nТесты для - " + checkedFile + ":\n" +
                    "ОШИБКА ТЕСТА                   - FAIL - int:ConversationID не совпадает с ID транзакции.\n");
    /*Если все ок, печатается лог проверки в консоль*/
    System.out.println("\nТесты для - " + checkedFile + ":\n" +
            "int:ConversationID             - PASSED - совпадает с ID транзакции.");

    /*Проверка наличия и заполненности тега csdo:EventDateTime*/
    assertTrue(Objects.requireNonNull(XPathBaseHelper.go(pathToLog + checkedFile,
            "//csdo:EventDateTime/text()")).length() != 0,
            "ОШИБКА ТЕСТА                   - FAIL - csdo:EventDateTime отсутствует или не заполнен.\n");
    /*Если все ок, печатается лог проверки в консоль*/
    System.out.println("csdo:EventDateTime             - PASSED - заполнен и присутствует.");

    /*Проверка наличия в соответствующем теге ответного сообщения ПРОП, правильного по постановке кода завершения транзакции
     *  Например :
     * для транзакции создания   - код равен "1"
     * для транзакции изменения  - код равен "2"
     * для транзакции исключения - код равен "3" и т.д*/
    assertEquals(resultCode, Objects.requireNonNull(XPathBaseHelper.go(pathToLog + checkedFile,
            "//" + tegCheckCode + "/text()")),
            "ОШИБКА ТЕСТА                   - FAIL - " + tegCheckCode + " содержит НЕверный код\n");
    /*Если все ок, печатается лог проверки в консоль*/
    System.out.println(tegCheckCode + "      - PASSED - содержит верный код \"" + resultCode + "\"");

    /*Проверка текста в соответсвующем теге ответного сообщения ПРОП, на соответствие с ожидаемым по постановке.
     * Например :
     * для транзакции создания   - "Сведения добавлены"
     * для транзакции изменения  - "Сведения изменены"
     * для транзакции исключения - "Сведения исключены" и т.д*/
    assertEquals(descriptionText, Objects.requireNonNull(XPathBaseHelper.go(pathToLog + checkedFile,
            "//" + tegDescriptionText + "/text()")),
            "ОШИБКА ТЕСТА                   - FAIL - " + tegDescriptionText + " НЕ соответствует значению.\n");
    /*Если все ок, печатается лог проверки в консоль*/
    System.out.println(tegDescriptionText + "           - PASSED - соответствует значению \"" + descriptionText + "\"");

  }


  protected void testAssert_For_Signal(String checkedFile) {

    /*Проверка что проверяемый файл присутствует в папке*/
    assertTrue(new File(pathToLog + checkedFile).exists(),
            "Тесты для - " + checkedFile + ":\n" +
                    "ОШИБКА ТЕСТА - В папке \n" + pathToLog + "\n" +
                    "отсутствует файл - " + checkedFile + "\n");

    /*Считывание ConversationID из сохраненного в Log после отправки инициирующего файла*/
    setConversationIdForAssert();

    /*Проверка сопадения ConversationID между ответным сообщением и инициирующим из папки Log*/
    assertEquals(conversationID, Objects.requireNonNull(XPathBaseHelper.go(pathToLog + checkedFile,
            "//int:ConversationID/text()")),
            "\nТесты для - " + checkedFile + ":\n" +
                    "ОШИБКА ТЕСТА                   - FAIL - int:ConversationID не совпадает с ID транзакции.\n");
    /*Если все ок, печатается лог проверки в консоль*/
    System.out.println("\nТесты для - " + checkedFile + ":\n" +
            "int:ConversationID             - PASSED - совпадает с ID транзакции.");

  }


  protected String getNameOfSaveInitMessage() {
    return nameOfSavedInitMessage;
  }

  protected void setNameOfSaveInitMessage(String nameOfSaveInitMessageUser) {
    nameOfSavedInitMessage = nameOfSaveInitMessageUser;
  }

  protected String getPathToInitMessage() {
    return pathToInitMessage;
  }

  protected void setPathToInitMessage(String pathToInitMessageUser) {
    pathToInitMessage = PATHCOMMON + pathToInitMessageUser;
  }

  protected String getPathToLog() {
    return pathToLog;
  }

  protected void setPathToLog(String pathToLogUser) {
    pathToLog = PATHCOMMON + pathToLogUser;
  }


}
