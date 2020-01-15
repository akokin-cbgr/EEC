package ru.cbgr.EEC;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import org.w3c.dom.Document;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import static com.ibm.mq.jms.JMSC.MQJMS_TP_CLIENT_MQ_TCPIP;

public class HelperBase {


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

  /*Общие поля после инициализации*/
  private Queue queueReciev;
  private QueueSender queueSender;
  private QueueReceiver queueReceiver;
  private QueueSession queueSession;
  private Queue queueSend;
  private QueueConnection queueConnection;

  /*Метод генерации UUID при помощи стандартной библиотеки из java.util*/
  private UUID uuid() {
    return UUID.randomUUID();
  }


  /*Генерация случайного числа из устанавливаемого диапазона значений в параметрах min и max соответственно
     Стандартно rand.nextInt() генерирует случайное число от 0 до указанного в параметре значения*/
  public int randInt(int min, int max) {
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
  public String onMessage(Message message) {
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
  public void clearQueue(QueueReceiver queueReceiver) {
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
        queueReceiver.receive();
        //Message message = queueReceiver.receive(100);// Обнуляем очередь от сообщений
      }
      browser.close();*/
  }


  /*Метод записи передаваемого или получаемого сообщения на HDD*/
  public void writeMsgToHdd(String fileInit, String filePath) throws IOException {
    //Запись отправляемого MSG в файл для статистики
    File file = new File(filePath);
    FileWriter writerInit = new FileWriter(file);
    writerInit.write(fileInit);
    writerInit.flush();
    writerInit.close();
  }


  /*Метод отправки инициализирубщего сообщения в соответствующую очередь MQ*/
  public void sendMsg(QueueSession queueSession, QueueSender queueSender, String fileInit) throws JMSException {
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
    System.out.println("Сообщение " + result + " отправлено.");
  }


  /*Вариант получения JMSCorrelationID*/
  //System.out.println("after sending a message we get message id "+ textMessage.getJMSMessageID());
  //String jmsCorrelationID = " JMSCorrelationID = '" + textMessage.getJMSMessageID() + "'";


  /*Метод подготовки XML к отправке в MQ, идет генерация UUID*/
  public String filePreparation(String filePath) {
    String fileRaw = getFile(filePath);//считываем из файла XML
    /*Производим замену UUID на сгенерированные*/
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</wsa:MessageID>", ">urn:uuid:" + uuid().toString() + "</wsa:MessageID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ConversationID>", ">urn:uuid:" + uuid().toString() + "</int:ConversationID>");
    fileRaw = fileRaw.replaceAll(">urn:uuid:.*</int:ProcedureID>", ">urn:uuid:" + uuid().toString() + "</int:ProcedureID>");
    fileRaw = fileRaw.replaceAll(">.*</csdo:EDocId>", ">" + uuid().toString() + "</csdo:EDocId>");
    return fileRaw;
  }


  /*Метод выборки значения из указанного файла XML согласно выражению xpath*/
  public String variableFromXml(String filepath, String xpath) {
    return XPathBaseHelper.go(filepath, xpath);
  }


  /*Метод форматирования XML в читаемый вид*/
  public String formatXml(String unFormatedXml) {
    Document document = XmlStringFormatter.convertStringToDocument(unFormatedXml);
    return XmlStringFormatter.toPrettyXmlString(document);
  }


  public void init(String hostName, String channel, int port, String queueManager, String queueSending, String queueRecieve) throws JMSException {
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


  /*Метод получения сообщения из определенной очереди MQ*/
  public StringBuilder receiveMsgFromQueue(int kolvoMSG, QueueSession queueSession, Queue queueReciev, String pathToLog) throws JMSException, IOException, InterruptedException {
    QueueBrowser browser = queueSession.createBrowser(queueReciev);//Создаем браузер для наблюдения за очередью
    Enumeration e = browser.getEnumeration();//получаем Enumeration
    StringBuilder stringBuilder = new StringBuilder();//создаем stringBuilder для записи в него сообщения из очереди
    StringBuilder result = new StringBuilder();// создаем stringBuilder для формирования строки консоли о типах полученных сообщений
    int i = 0;
    //i < kolvoMSG + 1
    /*Цикл вычитки и последующей записи в соответствующие файлы полученных в очереди сообщений*/
    while (e.hasMoreElements()) {
      Message message = (Message) e.nextElement(); //Получение сообщения
      stringBuilder.append(onMessage(message)).append("\n"); // запись в stringBuilder вычитанного сообщения
      /*Условия сортировки сообщений по типу*/
      if (stringBuilder.toString().contains("P.MSG.PRS")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_PRS.xml");
        result.append("- MSG.PRS\n");
      } else if (stringBuilder.toString().contains("P.MSG.RCV")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_RCV.xml");
        result.append("- MSG.RCV\n");
      } else if (stringBuilder.toString().contains("P.MSG.ERR")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_ERR.xml");
        result.append("- MSG.ERR\n");
      } else if (stringBuilder.toString().contains("MSG.002")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_002.xml");
        result.append("- MSG.002\n");
      } else if (stringBuilder.toString().contains("MSG.004")) {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_004.xml");
        result.append("- MSG.004\n");
      } else {
        writeMsgToHdd(formatXml(stringBuilder.toString().replaceAll("UTF", "utf")),
                pathToLog + "Received_MSG_XXX.xml");
        result.append("- MSG.XXX\n");
      }
      if (stringBuilder.toString().equals("")){
        System.out.println("IT`s VERY BAD because " +
                "no MSG in " + queueRecieve);
        return null;
      }
      i++;
      stringBuilder.delete(0, stringBuilder.length());
      //queueReceiver.receive();
      //String responseMsg = ((TextMessage) message).getText();
    }

    System.out.println("Получено: \n" + result); // формирование строки-отчета в консоли
    browser.close();
    return stringBuilder;
  }


  public void close() throws JMSException {
    /*Остановка*/
    getQueueSender().close();
    getQueueReceiver().close();
    getQueueSession().close();
    getQueueConnection().close();
  }


  public QueueReceiver getQueueReceiver() {
    return queueReceiver;
  }

  public void setQueueReceiver(QueueReceiver queueReceiver) {
    this.queueReceiver = queueReceiver;
  }

  public Queue getQueueReciev() {
    return queueReciev;
  }

  public void setQueueReciev(Queue queueReciev) {
    this.queueReciev = queueReciev;
  }

  public QueueSender getQueueSender() {
    return queueSender;
  }

  public void setQueueSender(QueueSender queueSender) {
    this.queueSender = queueSender;
  }

  public QueueSession getQueueSession() {
    return queueSession;
  }

  public void setQueueSession(QueueSession queueSession) {
    this.queueSession = queueSession;
  }

  public Queue getQueueSend() {
    return queueSend;
  }

  public void setQueueSend(Queue queueSend) {
    this.queueSend = queueSend;
  }

  public QueueConnection getQueueConnection() {
    return queueConnection;
  }

  public void setQueueConnection(QueueConnection queueConnection) {
    this.queueConnection = queueConnection;
  }

  public String getPathCommon() {
    return pathCommon;
  }

  public void setPathCommon(String pathCommon) {
    this.pathCommon = pathCommon;
  }

  public String getOpName() {
    return opName;
  }

  public void setOpName(String opName) {
    this.opName = opName;
  }

  public String getTipMSG() {
    return tipMSG;
  }

  public void setTipMSG(String tipMSG) {
    this.tipMSG = tipMSG;
  }

  public String getTipTRN() {
    return tipTRN;
  }

  public void setTipTRN(String tipTRN) {
    this.tipTRN = tipTRN;
  }

  public String getNumberMSG() {
    return numberMSG;
  }

  public void setNumberMSG(String numberMSG) {
    this.numberMSG = numberMSG;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getQueueManager() {
    return queueManager;
  }

  public void setQueueManager(String queueManager) {
    this.queueManager = queueManager;
  }

  public String getQueueSending() {
    return queueSending;
  }

  public void setQueueSending(String queueSending) {
    this.queueSending = queueSending;
  }

  public String getQueueRecieve() {
    return queueRecieve;
  }

  public void setQueueRecieve(String queueRecieve) {
    this.queueRecieve = queueRecieve;
  }

}
