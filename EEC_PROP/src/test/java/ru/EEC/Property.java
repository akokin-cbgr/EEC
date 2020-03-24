package ru.EEC;

class Property {

  /*Переменные настройки подключения к шлюзу*/
  static final String HOSTNAME = "eek-test1-ip-mq-sync.tengry.com";       //Адресс шлюза SYNC;
  static final String CHANNEL = "ESB.SVRCONN";                            //Канал
  static final int PORT = 1414;                                           //Порт
  static final String QUEUEMANAGER = "SYNC.IIS.QM";                       //Менеджер очередей SYNC
  static final String QUEUESENDING = "ADP.PROP.IN";                       //Очередь для отправки сообщений
  static final String QUEUERECIVE = "Q.ADDR1";                            //Тупиковая очередь для ответных сообщений

  /*Поля с путями к файлам*/
  static final String PATHCOMMON = "src/main/resources/";

}
