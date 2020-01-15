package ru.cbgr.EEC;

public class WorkWithMQ  {


  public static void main(String[] args) {
/*
    try {
      //устанавливаем параметры подключения
      MQQueueConnectionFactory mqQueueConnectionFactory = new MQQueueConnectionFactory();
      mqQueueConnectionFactory.setHostName("eek-test1-ip-mq1.tengry.com");
      mqQueueConnectionFactory.setChannel("ESB.SVRCONN");
      mqQueueConnectionFactory.setPort(1414);
      mqQueueConnectionFactory.setQueueManager("RU.IIS.QM");
      mqQueueConnectionFactory.setTransportType(MQJMS_TP_CLIENT_MQ_TCPIP);
      QueueConnection queueConnection = mqQueueConnectionFactory.createQueueConnection("", "");//создаем соединение и запускаем сессию
      queueConnection.start();
      QueueSession queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
      Queue queueSend = queueSession.createQueue("GATEWAY.EXT.IN");
      Queue queueReciev = queueSession.createQueue("Q.ADDR5");
      QueueSender queueSender = queueSession.createSender(queueSend);//указываем в какую очередь отправить сообщение
      QueueReceiver queueReceiver = queueSession.createReceiver(queueReciev);//указываем очередь откуда читать ответное сообщение

      clearQueue(queueReceiver);

      //String myStr = getFile("OP_02/FLC/MSG.001_TRN.001/FLC_01.xml");//считываем из файла XML
      String fileInit = filePreparation("OP_02/FLC/MSG.001_TRN.001/MSG.001.xml");

      fileInit = fileInit.replaceAll(">.*</casdo:BorderCheckPointCode>", ">PPG.RU.UA." + randInt(10000000, 99999999) + "</casdo:BorderCheckPointCode>");

      writeMsgToHdd(fileInit, "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/MSG_01.xml");

      sendMsg(queueSession, queueSender, fileInit);

      Thread.sleep(4000);//задержка на получение ответа от ПРОП

      StringBuilder stringBuilder = receiveMsgFromQueue(queueSession, queueReciev);

      //System.out.println("Сообщение получено \n " + test);
      //boolean bool = test.toString().contains("<sgn:Description>Ошибка контроля</sgn:Description>");
      //System.out.println("Проверка - " + bool);

      clearQueue(queueReceiver);

      writeMsgToHdd(stringBuilder.toString(), "src/main/resources/OP_02/FLC/MSG.001_TRN.001/Log/01.xml");


      queueSender.close();
      queueReceiver.close();
      queueSession.close();
      queueConnection.close();


    } catch (Exception e) {
      e.printStackTrace();
    }*/
  }

}