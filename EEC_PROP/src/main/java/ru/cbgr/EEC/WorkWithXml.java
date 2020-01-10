package ru.cbgr.EEC;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class WorkWithXml {
  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
   /* try(FileReader reader = new FileReader("D://MSG_2.xml"))
    {
      // читаем посимвольно
      int c;
      while((c=reader.read())!=-1){

        System.out.print((char)c);
      }
    }
    catch(IOException ex){

      System.out.println(ex.getMessage());
    }*/
    // Создается построитель документа
    //DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    // Создается дерево DOM документа из файла
    //Document document = documentBuilder.parse("D://MSG_2.xml");
    //Node root = document.getDocumentElement();
    //NodeList books = root.getChildNodes();
    //System.out.println(books.item(1) + books.item(1).getNodeName() +  " : " + books.item(1).getTextContent());

    File file = new File("D://MSG_2.xml");
    BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
    System.out.println(bufferedReader.toString().toCharArray());
    bufferedReader.close();

  }

}
