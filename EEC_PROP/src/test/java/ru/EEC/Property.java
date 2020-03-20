package ru.EEC;

public class Property {

  /*Переменные настройки подключения к шлюзу*/
   String hostName = "eek-test1-ip-mq-sync.tengry.com";       //Адресс шлюза SYNC;
   String channel  = "ESB.SVRCONN";                           //Канал
   int port = 1414;                                           //Порт
   String queueManager = "SYNC.IIS.QM";                       //Менеджер очередей SYNC
   String queueSending = "ADP.PROP.IN";                       //Очередь для отправки сообщений
   String queueRecieve = "Q.ADDR1";                           //Тупиковая очередь для ответных сообщений

  /*Поля с путями к файлам*/
   final String pathCommon = "src/main/resources/";
   String pathToInitMessage;
   String pathToLog;

}
