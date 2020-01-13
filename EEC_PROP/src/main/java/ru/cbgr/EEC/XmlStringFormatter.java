package ru.cbgr.EEC;

import net.sf.saxon.TransformerFactoryImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

class XmlStringFormatter {

  /*public static void main(String args[]) {
    // какая-то строка с неформатированным XML содержимым
    String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
            "\n<Developer id=\"1\">\n" +
            "<name>Andrew</name><age>25</age><position>Middle</position>" +
            "<language>Java</language></Developer>";

    Document document = convertStringToDocument(xmlString);

    // обычно используют indent = 4, но посмотрим пример с 6
    System.out.println(toPrettyXmlString(6, document));
  }*/

  // в переменной indent указываем уровень(величину) отступа
  static String toPrettyXmlString(Document document) {
    try {
      // удаляем пробелы
      XPath xPath = XPathFactory.newInstance().newXPath();
      NodeList nodeList = (NodeList) xPath.evaluate(
              "//text()[normalize-space()='']",
              document,
              XPathConstants.NODESET
      );

      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        node.getParentNode().removeChild(node);
      }

      // устанавливаем настройки для красивого форматирования
      TransformerFactory transformerFactory = new TransformerFactoryImpl();
      //transformerFactory.setAttribute("indent-number", indent);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      // форматируем XML
      StringWriter stringWriter = new StringWriter();
      transformer.transform(new DOMSource(document), new StreamResult(stringWriter));

      // возвращаем строку с отформатированным XML
      return stringWriter.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // метод для конвертации строки с XML разметкой в объект Document
  static Document convertStringToDocument(String xml) {
    try {
      return DocumentBuilderFactory.newInstance()
              .newDocumentBuilder()
              .parse(new InputSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    } catch (SAXException | IOException | ParserConfigurationException e) {
      e.printStackTrace();
    }
    return null;
  }

}