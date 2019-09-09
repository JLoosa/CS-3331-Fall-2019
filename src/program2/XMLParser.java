package program2;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParser {

    public static void main(String[] args) {

	try {
	    File inputFile = new File("depends/newTest.xml");
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(inputFile);
	    doc.getDocumentElement().normalize();
	    System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
	    NodeList nList = doc.getElementsByTagName("student");
	    System.out.println("----------------------------");

	    for (int temp = 0; temp < nList.getLength(); temp++) {
		Node nNode = nList.item(temp);
		System.out.println("\nCurrent Element :" + nNode.getNodeName());

		if (nNode.getNodeType() == Node.ELEMENT_NODE) {
		    Element eElement = (Element) nNode;
		    System.out.println("Student roll no : " + eElement.getAttribute("rollno"));
		    System.out.println(
			    "First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
		    eElement.getElementsByTagName("firstname").item(0).setTextContent("Fred Lives");
		    System.out.println(
			    "Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
		    System.out.println(
			    "Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
		    System.out.println("Marks : " + eElement.getElementsByTagName("marks").item(0).getTextContent());
		}
	    }

	    // write the content into xml file
	    TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    Transformer transformer = transformerFactory.newTransformer();
	    DOMSource source = new DOMSource(doc);
	    StreamResult result = new StreamResult(new File("depends/newTest.xml"));
	    transformer.transform(source, result);

	    // Output to console for testing
	    StreamResult consoleResult = new StreamResult(System.out);
	    transformer.transform(source, consoleResult);

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
