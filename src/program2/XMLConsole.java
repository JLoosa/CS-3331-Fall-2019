package program2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Just so you know, Professor, we wouldn't have been upset if you told us that
 * you nicked the code for the parser example off of StackOverflow. That said,
 * it is pretty funny.
 * https://stackoverflow.com/questions/428073/what-is-the-best-simplest-way-to-read-in-an-xml-file-in-java-application
 * 
 * Submission for program 2 for CS 3331
 * 
 * <ul>
 * <li>9/7/2019: Initial file, found StackOverflow post. Fun day</li>
 * <li>9/7/2019: Minor updates to comments and removed one line of useless debug</li>
 * </ul>
 * 
 * @author Jacob Loosa
 *
 */
public class XMLConsole {

    static String fileName;
    static Scanner keyboard;
    static File file;

    DocumentBuilderFactory documentBuilderFactory;
    DocumentBuilder documentBuilder;
    Document document;

    Element currentElementNode;
    List<Element> currentElementChildren;
    String lastCommandString;

    public XMLConsole() {
	documentBuilderFactory = DocumentBuilderFactory.newInstance();
	try {
	    documentBuilder = documentBuilderFactory.newDocumentBuilder();
	} catch (ParserConfigurationException e) {
	    System.err.println("An exception occered while making the Document Builder: " + e.getMessage());
	    System.exit(1);
	}
	try {
	    document = documentBuilder.parse(file);
	    document.getDocumentElement().normalize();
	} catch (SAXException | IOException e) {
	    System.err.println("An exception occered while making the Document: " + e.getMessage());
	    System.exit(1);
	}
	currentElementNode = document.getDocumentElement();
	// We are assuming that it is in the form root -> child -> attribute things*
	currentElementNode = (Element) currentElementNode.getChildNodes().item(1);

	// Main loop of program. We will always show the current line and a carrot for typing input
	while (true) {
	    showCurrentLine();
	    System.out.print(">");
	    String input = keyboard.nextLine();
	    parseCommand(input);
	}
    }

    /**
     * Used for the previous and next command. This one "scrolls" forwards
     * @return
     */
    Element nextNode() {
	Node temp = currentElementNode;
	while (temp == currentElementNode || (temp != null && temp.getNodeType() != Node.ELEMENT_NODE))
	    temp = temp.getNextSibling();
	return temp != null ? (Element) temp : null;
    }


    /**
     * Used for the previous and next command. This one "scrolls" backwards
     * @return
     */
    Element prevNode() {
	Node temp = currentElementNode;
	while (temp == currentElementNode || (temp != null && temp.getNodeType() != Node.ELEMENT_NODE))
	    temp = temp.getPreviousSibling();
	return temp != null ? (Element) temp : null;
    }

    /**
     * Used to find all of the children so we can add them to the text we show the user
     * @param parent
     * @return
     */
    List<Element> getElementChildren(Element parent) {
	if (parent.getChildNodes().getLength() == 0)
	    return new LinkedList<Element>();
	List<Element> list = new LinkedList<Element>();
	for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
	    if (parent.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
		list.add((Element) parent.getChildNodes().item(i));
	    }
	}
	return list;
    }

    boolean showCurrentLine() {
	String tagName = currentElementNode.getTagName();
	currentElementChildren = getElementChildren(currentElementNode);
	String[] childText = new String[currentElementChildren.size()];
	for (int index = 0; index < childText.length; index++) {
	    childText[index] = String.join("=", currentElementChildren.get(index).getTagName(),
		    currentElementChildren.get(index).getChildNodes().item(0).getTextContent());
	}
	String out = String.format("%s: %s", tagName, String.join(", ", childText));
	System.out.println(out);
	return true;
    }

    void parseCommand(String command) {
	if (command == null || command.isEmpty())
	    return;
	String[] segments = command.split(" ");
	command = segments[0].toUpperCase();
	segments = Arrays.copyOfRange(segments, 1, segments.length);
	// Show command
	if (command.matches("S(HOW)?")) {
	    // Nothing to do here. It always shows
	    return;
	}
	// Change command
	if (command.matches("C(HANGE)?")) {
	    if (segments.length < 2) {
		System.err.println("Please provide more information. Usage: CHANGE <Tag> <Value>");
	    } else {
		String tag = segments[0];
		String value = segments.length == 2 ? segments[1]
			: String.join(" ", Arrays.copyOfRange(segments, 1, segments.length));
		// Sequential search to edit values
		for (Element el : currentElementChildren) {
		    if (el.getTagName().equals(tag)) {
			el.getChildNodes().item(0).setTextContent(value);
			return;
		    } else
			continue;
		}
		System.err.println("Unable to find tag. Remember that XML is case-sensitive");
	    }
	    return;
	}
	// Write command
	if (command.matches("W(RITE)?")) {
	    if (segments.length < 1) {
		System.err.println("Please provide more information.\n\t WRITE <Destination>");
	    } else {
		String dest = segments[0];
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
		    transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
		    System.err.println("Unable to save file: " + e.getMessage());
		    return;
		}
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(new File(dest));
		try {
		    transformer.transform(source, result);
		} catch (TransformerException e) {
		    System.err.println("Unable to save file: " + e.getMessage());
		    return;
		}
	    }
	    System.out.println("file saved.");
	    return;
	}
	// Next command
	if (command.matches("N(EXT)?")) {
	    Element next = nextNode();
	    if (next == null) {
		System.err.println("You are at the end of the file.");
	    } else {
		currentElementNode = next;
	    }
	    return;
	}
	// Previous command
	if (command.matches("P(REVIOUS)?")) {
	    Element next = prevNode();
	    if (next == null) {
		System.err.println("You are at the start of the file.");
	    } else {
		currentElementNode = next;
	    }
	    return;
	}
	// Exit command
	if (command.matches("E(XIT)?")) {
	    System.out.println("Exiting...");
	    // I used an infinite loop at the start so this is honestly the best way to handle it
	    System.exit(0);
	    return;
	}
	System.out.println("Unknown command");
    }

    /**
     * The main launcher for the program
     * 
     * @param args first string should be a file location. If it does not exist, the
     *             user is prompted
     */
    public static void main(String[] args) {
	if (args.length > 0)
	    fileName = args[0];
	keyboard = new Scanner(System.in);
	if (fileName == null) {
	    System.out.print("Please provide the file location \n >> ");
	    fileName = keyboard.nextLine();
	}
	file = new File(fileName);
	if (!file.canRead()) {
	    System.err.println("That file does not exist or cannot be read");
	    System.exit(1);
	}
	System.out.println("File location is set to: " + fileName);
	// We know that we have a file, let's try to start the console
	new XMLConsole();
    }

}
