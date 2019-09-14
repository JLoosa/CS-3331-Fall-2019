package program3;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Adaptation of my code for Program 2. This one is configured to search through
 * the entire passed XML file and to then extract all of the information needed
 * to create the segments. The code is simplified due to guarantees provided
 * within the project scope
 * 
 * @author Jacob Loosa
 *
 */
public class CarXMLReader {
    private File file;

    private DocumentBuilderFactory documentBuilderFactory;
    private DocumentBuilder documentBuilder;
    private Document document;

    private Element currentElementNode, nextElementNode;
    private List<Element> currentElementChildren;

    private String segmentLengthStr, segmentSpeedStr, segmentNumberStr;
    private int segmentNumber;
    private float segmentLength, segmentSpeed;

    public CarXMLReader(String fileName) {
	file = new File(fileName);
	if (!file.canRead()) {
	    System.err.println("That file does not exist or cannot be read");
	    System.exit(1);
	}
	System.out.println("File location is set to: " + fileName);
	// We know that we have a file, let's try to read it

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
	// We are assuming that it is in the form root -> child -> attribute things
	currentElementNode = (Element) currentElementNode.getChildNodes().item(1);
	do {
	    Car.printDebug("Loading Segment");
	    nextElementNode = nextNode();
	    currentElementChildren = getElementChildren(currentElementNode);
	    // Set these to null so we know if the data is actually provided
	    segmentLengthStr = null;
	    segmentSpeedStr = null;
	    segmentNumberStr = null;
	    boolean err = false;

	    // Loop through each element and identify which node it is. If we find a
	    // malformed node, tell the user and skip it. Note that skipping will result in
	    // an incomplete set of nodes, so the code should not execute
	    for (Element eNode : currentElementChildren) {
		try {
		    if (eNode.getTagName().equals("SEGMENT_NUMBER")) {
			segmentNumberStr = eNode.getChildNodes().item(0).getTextContent();
			segmentNumber = Integer.parseInt(segmentNumberStr);
		    } else if (eNode.getTagName().equals("LENGTH")) {
			segmentLengthStr = eNode.getChildNodes().item(0).getTextContent();
			segmentLength = Kinematics.toFeet(Float.parseFloat(segmentLengthStr));
			if (segmentLength < 0.5 * Kinematics.toFeet(1)) {
			    System.err.println("Segment length is less than the minimum of 0.5 and is therefore invalid!");
			    err = true;
			}
		    } else if (eNode.getTagName().equals("SPEED_LIMIT")) {
			segmentSpeedStr = eNode.getChildNodes().item(0).getTextContent();
			segmentSpeed = Kinematics.toFeetPerSecond(Float.parseFloat(segmentSpeedStr));
		    } else {
			throw new RuntimeException("Unknown Node Tag: " + eNode.getTagName());
		    }
		} catch (Exception exc) {
		    System.err.println("An error occured while parsing an Element node! " + exc.getMessage());
		    err = true;
		}
	    }
	    if (err || segmentLengthStr == null || segmentNumberStr == null || segmentSpeedStr == null) {
		System.err.println("Incomplete or erroneous segment in XML. Skipping: Program may fail.");
	    } else {
		Segment segment = new Segment(segmentNumber, segmentLength, segmentSpeed);
		Car.segments.add(segment);
		Car.printDebug("Segment " + segmentNumber + " Added.");
	    }

	    currentElementNode = nextElementNode;
	} while (nextElementNode != null);

    }

    /**
     * Used for the previous and next command. This one "scrolls" forwards
     * 
     * @return
     */
    private Element nextNode() {
	Node temp = currentElementNode;
	while (temp == currentElementNode || (temp != null && temp.getNodeType() != Node.ELEMENT_NODE))
	    temp = temp.getNextSibling();
	return temp != null ? (Element) temp : null;
    }

    /**
     * Used to find all of the children so we can add them to the text we show the
     * user
     * 
     * @param parent
     * @return
     */
    private List<Element> getElementChildren(Element parent) {
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

}
