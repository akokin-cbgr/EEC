import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.w3c.dom.Document;
import ru.cbgr.EEC.XPathBaseHelper;
import ru.cbgr.EEC.XmlStringFormatter;

import javax.jms.Queue;
import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;

public class TestBase {


  /*Переменные настройки подключения к шлюзу*/
  private String hostName;
  private String channel;
  private int port;
  private String queueManager;
  private String queueSending;
  private String queueRecieve;

  /*Поля с путями к файлам*/
  private String pathCommon = "src/main/resources/";
  private String opName;
  private String tipMSG;
  private String tipTRN;
  private String numberMSG;
  private String pathToInitMessage;
  private String pathToLog;

  /*Общие поля после инициализации*/

  private Queue queueReciev;
  private QueueSender queueSender;
  private QueueReceiver queueReceiver;
  private QueueSession queueSession;
  private Queue queueSend;
  private QueueConnection queueConnection;

  /*Переменные для assert`ов*/
  private String conversationID = "";


  /*Метод генерации UUID при помощи стандартной библиотеки из java.util*/
  private UUID uuid() {
    return UUID.randomUUID();
  }


  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
     Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  int randInt(int min, int max) {
    Random rand = new Random();
    return rand.nextInt((max - min) + 1) + min;
  }


  /*Генерация строки определенной длинны из определенного набора символов
      diapazon - набор символов из которых будет генерироваться строка
      kol_vo - длинна генерируемой строки
  */
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


  /*Метод считывания файла с HDD в строку*/
  private String getFile(String fileName) {
    StringBuilder result = new StringBuilder();
    if (new File(fileName).exists()) {
      File file = new File(fileName);
      try (Scanner scanner = new Scanner(file)) {
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
          result.append(line).append("\n");
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      return "File not found!";
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
  void clearQueue(QueueReceiver queueReceiver) {
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

  /*Метод удаления всех файлов из папки*/
  void deleteAllFilesFolder(String path) {
    for (File myFile : Objects.requireNonNull(new File(path).listFiles()))
      if (myFile.isFile()) {
        myFile.delete();
      }
  }


  /*Метод записи передаваемого или получаемого сообщения на HDD*/
  void writeMsgToHdd(String fileInit, String filePath) throws IOException {
    //Запись отправляемого MSG в файл для статистики
    File file = new File(filePath);
    FileWriter writerInit = new FileWriter(file);
    writerInit.write(fileInit);
    writerInit.flush();
    writerInit.close();
  }


  /*Метод отправки инициализирубщего сообщения в соответствующую очередь MQ*/
  void sendMsg(QueueSession queueSession, QueueSender queueSender, String fileInit) throws JMSException {
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах полученных сообщений
    TextMessage textMessage = queueSession.createTextMessage(fileInit);

    /*в случае необходимости устанавливаем параметры для отправляемого сообщения*/
    //textMessage.setJMSReplyTo(queueReciever);
    //textMessage.setJMSType("mcd://xmlns");//message type
    //textMessage.setJMSExpiration(50*1000);//message expiration
    //textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT); //message delivery mode either persistent or non-persistemnt
    //queueSender.setTimeToLive(50*1000);// установка времени жизни сообщения

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
    System.out.println("Сообщение " + result + " отправлено:\n" +
            "- Очередь           - " + queueSending +
            "\n- Адрес шлюза       - " + hostName + "\n");
  }



  /*Вариант получения JMSCorrelationID*/
  //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
  //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";

  /*Метод подготовки XML к отправке в MQ, идет генерация UUID*/
  String filePreparation(String filePath) {
    if (getFile(filePath).equals("\"File not found!\"")) {
      return "File not found!";
    }
    String fileRaw = getFile(filePath);//считываем из файла XML
    /*Производим замену UUID на сгенерированные*/
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
    fileRaw = fileRaw.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");
    return fileRaw;
  }


  /*Метод выборки значения из указанного файла XML согласно выражению xpath*/
  String variableFromXml(String filepath, String xpath) {
    return XPathBaseHelper.go(filepath, xpath);
  }


  /*Метод форматирования XML в читаемый вид*/
  private String formatXml(String unFormatedXml) {
    Document document = XmlStringFormatter.convertStringToDocument(unFormatedXml);
    return XmlStringFormatter.toPrettyXmlString(document);
  }

  void init(String hostName, String channel, int port, String queueManager, String queueSending, String queueRecieve) throws JMSException {
    //устанавливаем параметры подключения
    MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
    mqQueueConnectionFactory.setHostName(hostName);
    mqQueueConnectionFactory.setChannel(channel);
    mqQueueConnectionFactory.setPort(port);
    mqQueueConnectionFactory.setQueueManager(queueManager);
    mqQueueConnectionFactory.setTransportType(MQJMS_TP_CLIENT_MQ_TCPIP);
    //создаем соединение и запускаем сессию
    queueConnection = mqQueueConnectionFactory.createQueueConnection("", "");
    getQueueConnection().start();
    queueSession = getQueueConnection().createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
    /*Создаем очереди отправки и получения*/
    queueSend = getQueueSession().createQueue(queueSending);
    queueReciev = getQueueSession().createQueue(queueRecieve);
    //указываем в какую очередь отправить сообщение
    queueSender = getQueueSession().createSender(getQueueSend());
    //указываем очередь откуда читать ответное сообщение
    queueReceiver = getQueueSession().createReceiver(getQueueReciev());
  }


  void checkAndWaitMsgInQueue(int maxWaitTimeSec) throws JMSException, InterruptedException {
    QueueBrowser browser = this.queueSession.createBrowser(this.queueReciev);//Создаем браузер для наблюдения за очередью
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
  StringBuilder receiveMsgFromQueue(QueueSession queueSession, Queue queueReciev, String pathToLog) throws JMSException, IOException {
    QueueBrowser browser = queueSession.createBrowser(queueReciev);//Создаем браузер для наблюдения за очередью
    Enumeration e = browser.getEnumeration();//получаем Enumeration
    StringBuilder stringBuilder = new StringBuilder();//создаем stringBuilder для записи в него сообщения из очереди
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах полученных сообщений
    int i = 1;
    /*Цикл вычитки и последующей записи в соответствующие файлы полученных в очереди сообщений*/
    while (e.hasMoreElements()) {
      Message message = (Message) e.nextElement(); //Получение сообщения
      stringBuilder.append(onMessage(message)).append("\n"); // запись в stringBuilder вычитанного сообщения
      /*Условия сортировки сообщений по типу*/
      if (stringBuilder.toString().contains("P.MSG.PRS</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_PRS.xml");
        result.append("- MSG.PRS\n");
      } else if (stringBuilder.toString().contains("P.MSG.RCV</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_RCV.xml");
        result.append("- MSG.RCV\n");
      } else if (stringBuilder.toString().contains("P.MSG.ERR</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_ERR.xml");
        result.append("- MSG.ERR\n");
      } else if (stringBuilder.toString().contains("MSG.002</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_002.xml");
        result.append("- MSG.002\n");
      } else if (stringBuilder.toString().contains("MSG.004</wsa:Action>")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_004.xml");
        result.append("- MSG.004\n");
      } else {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_XXX.xml");
        result.append("- MSG.XXX\n");
      }
      i++;
      /*Очистка stringBuilder*/
      stringBuilder.delete(0, stringBuilder.length());

    }
    /*У словие возврата null если stringBuilder будет пустой по причине отсутствия сообщений (к примеру если ПРОП не ответил)
     * дополнительно будет сообщение о том что сообщений нет в в тупиковой очереди*/
    if (!(e.hasMoreElements()) & stringBuilder.length() == 0 & i == 1) {
      System.out.println("ИТОГ\n" +
              "ОШИБКА ТЕСТА - Очередь " + queueRecieve + " не содержит сообщений."); // формирование строки-отчета в консоли
      return null;
    }
    //queueReceiver.receive();
    //String responseMsg = ((TextMessage) message).getText();
    System.out.println("ИТОГ\n" +
            "Получено и созданно в " + this.getPathToLog() + " : \n" + result); // формирование строки-отчета в консоли
    browser.close();
    return stringBuilder;
  }


  void close() throws JMSException {
    /*Остановка*/
    getQueueSender().close();
    getQueueReceiver().close();
    getQueueSession().close();
    getQueueConnection().close();
  }


  private void setConversationIdForAssert() {
    if (new File(this.getPathToLog() + "Init_MSG_001.xml").exists()) {
      setConversationID(variableFromXml(this.getPathToLog() + "Init_MSG_001.xml", "//int:ConversationID/text()"));
    }
  }

  Boolean checkFileExist(String fileName) {
    return new File(this.getPathToLog() + fileName).exists();
  }


  String testAssert_For_Reply_Msg(String checkedFile, String tegCheckCode, String tegDescriptionText) {
    if (new File(this.getPathToLog() + checkedFile).exists()) {
      setConversationIdForAssert();
      String str = "";
      if (Objects.requireNonNull(XPathBaseHelper.go(this.getPathToLog() + checkedFile,
              "//int:ConversationID/text()")).equals(this.getConversationID())) {
        System.out.println("Тесты для - " + checkedFile + ":\n" +
                "int:ConversationID             - PASSED - совпадает с ID транзакции");
        str = "Passed";
      } else {
        System.out.println("Тесты для - " + checkedFile + ":\n" +
                "ОШИБКА ТЕСТА                   - FAIL - int:ConversationID не совпадает с ID транзакции");
        str = "NOT Passed";
      }
      if (Objects.requireNonNull(XPathBaseHelper.go(this.getPathToLog() + checkedFile,
              "//" + tegCheckCode + "/text()")).equals("3")) {
        System.out.println(
                tegCheckCode + "      - PASSED - содержит верный код \"3\"");
        str = "Passed";
      } else {
        System.out.println(
                "ОШИБКА ТЕСТА                   - FAIL - " + tegCheckCode + " содержит НЕверный код");
        str = "NOT Passed";
      }
      if (Objects.requireNonNull(XPathBaseHelper.go(this.getPathToLog() + checkedFile,
              "//" + tegDescriptionText + "/text()")).equals("Сведения добавлены")) {
        System.out.println(
                tegDescriptionText + "           - PASSED - соответствует значению \"Сведения добавлены\"");
        str = "Passed";
      } else {
        System.out.println(
                "ОШИБКА ТЕСТА                   - FAIL - " + tegDescriptionText + " НЕ соответствует значению");
        str = "NOT Passed";
      }
      return str;
    } else {
      System.out.println("Тесты для - " + checkedFile + ":\n" +
              "ОШИБКА ТЕСТА - В папке \n" + this.getPathToLog() + "\n" +
              "отсутствует файл - " + checkedFile + ":\n");
      return "NOT Passed";
    }
  }

  public String testAssert_For_Signal(String checkedFile) {
    if (new File(this.getPathToLog() + checkedFile).exists()) {
      setConversationIdForAssert();
      String str = "";
      if (Objects.requireNonNull(XPathBaseHelper.go(this.getPathToLog() + checkedFile,
              "//int:ConversationID/text()")).equals(this.getConversationID())) {
        System.out.println("Тесты для - " + checkedFile + ":\n" +
                "int:ConversationID             - PASSED - совпадает с ID транзакции\n");
        str = "Passed";
      } else {
        System.out.println("Тесты для - " + checkedFile + ":\n" +
                "ОШИБКА ТЕСТА                   - FAIL - int:ConversationID не совпадает с ID транзакции\n");
        str = "NOT Passed";
      }
      return str;
    } else {
      System.out.println("Тесты для - " + checkedFile + ":\n" +
              "ОШИБКА ТЕСТА - В папке \n" + this.getPathToLog() + "\n" +
              "отсутствует файл - " + checkedFile + ":\n");
      return "NOT Passed";
    }
  }


  QueueReceiver getQueueReceiver() {
    return queueReceiver;
  }

  public void setQueueReceiver(QueueReceiver queueReceiver) {
    this.queueReceiver = queueReceiver;
  }

  Queue getQueueReciev() {
    return queueReciev;
  }

  public void setQueueReciev(Queue queueReciev) {
    this.queueReciev = queueReciev;
  }

  QueueSender getQueueSender() {
    return queueSender;
  }

  public void setQueueSender(QueueSender queueSender) {
    this.queueSender = queueSender;
  }

  QueueSession getQueueSession() {
    return queueSession;
  }

  public void setQueueSession(QueueSession queueSession) {
    this.queueSession = queueSession;
  }

  private Queue getQueueSend() {
    return queueSend;
  }

  public void setQueueSend(Queue queueSend) {
    this.queueSend = queueSend;
  }

  private QueueConnection getQueueConnection() {
    return queueConnection;
  }

  public void setQueueConnection(QueueConnection queueConnection) {
    this.queueConnection = queueConnection;
  }

  String getPathCommon() {
    return pathCommon;
  }

  public void setPathCommon(String pathCommon) {
    this.pathCommon = pathCommon;
  }

  String getOpName() {
    return opName;
  }

  void setOpName(String opName) {
    this.opName = opName;
  }

  String getTipMSG() {
    return tipMSG;
  }

  void setTipMSG(String tipMSG) {
    this.tipMSG = tipMSG;
  }

  String getTipTRN() {
    return tipTRN;
  }

  void setTipTRN(String tipTRN) {
    this.tipTRN = tipTRN;
  }

  String getNumberMSG() {
    return numberMSG;
  }

  void setNumberMSG(String numberMSG) {
    this.numberMSG = numberMSG;
  }

  String getHostName() {
    return hostName;
  }

  void setHostName(String hostName) {
    this.hostName = hostName;
  }

  String getChannel() {
    return channel;
  }

  void setChannel(String channel) {
    this.channel = channel;
  }

  int getPort() {
    return port;
  }

  void setPort(int port) {
    this.port = port;
  }

  String getQueueManager() {
    return queueManager;
  }

  void setQueueManager(String queueManager) {
    this.queueManager = queueManager;
  }

  String getQueueSending() {
    return queueSending;
  }

  void setQueueSending(String queueSending) {
    this.queueSending = queueSending;
  }

  String getQueueRecieve() {
    return queueRecieve;
  }

  void setQueueRecieve(String queueRecieve) {
    this.queueRecieve = queueRecieve;
  }

  String getConversationID() {
    return conversationID;
  }

  void setConversationID(String conversationID) {
    this.conversationID = conversationID;
  }

  String getPathToInitMessage() {
    return this.pathToInitMessage = this.getPathCommon() + this.getOpName() + "/" + this.getTipMSG() + "/" + this.getTipTRN() + "/" + this.getNumberMSG();
  }

  void setPathToInitMessage(String pathToInitMessage) {
    this.pathToInitMessage = pathToInitMessage;
  }

  String getPathToLog() {
    return this.pathToLog = this.getPathCommon() + this.getOpName() + "/" + this.getTipMSG() + "/" + this.getTipTRN() + "/" + "Log/";
  }

  void setPathToLog(String pathToLog) {
    this.pathToLog = pathToLog;
  }


}
