package org.lodder.subtools.sublibrary.xml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.lodder.subtools.sublibrary.util.http.HttpClient;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

	private static String XMLCleanup(String text) {
		return text.replace("&amp;", "&");
	}

	private static String HTMLCleanup(String text) {
		return StringUtils.unescapeHTML(text);
	}

	public static String getStringTagValue(String sTag, Element eElement) {
		LOGGER.trace("getStringTagValue: sTag [{}]", sTag);
		return HTMLCleanup(XMLCleanup(getStringTagRawValue(sTag, eElement)));
	}

	public static String getStringTagRawValue(String sTag, Element eElement) {
		LOGGER.trace("getStringTagRawValue: sTag [{}]", sTag);
		if (eElement.getElementsByTagName(sTag).getLength() > 0) {
			NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
			Node nValue = nlList.item(0);

			if (nValue == null) {
				return "";
			} else {
				return nValue.getNodeValue();
			}
		}
		return "";
	}

	public static String getStringAtributeValue(String sTag, String sAtribute, Element eElement) {
		LOGGER.trace("getStringAtributeValue: sTag [{}], sAtribute [{}]", sTag, sAtribute);
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		return XMLCleanup(((Element) nlList).getAttribute(sAtribute));
	}

	public static int getIntTagValue(String sTag, Element eElement) {
		LOGGER.trace("getIntTagValue: sTag [{}]", sTag);
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = nlList.item(0);

		if (nValue == null) {
			return 0;
		} else {
			return Integer.parseInt(nValue.getNodeValue());
		}

	}

	public static boolean getBooleanTagValue(String sTag, Element eElement) {
		LOGGER.trace("getBooleanTagValue: sTag [{}]", sTag);
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = nlList.item(0);

		return nValue != null && Boolean.parseBoolean(nValue.getNodeValue());
	}

	public static boolean getBooleanAtributeValue(String sTag, String sAtribute, Element eElement) {
		LOGGER.trace("getBooleanAtributeValue: sTag [{}], sAtribute [{}]", sTag, sAtribute);
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		return ((Element) nlList).getAttribute(sAtribute) != null
				&& Boolean.parseBoolean(((Element) nlList).getAttribute(sAtribute));
	}

	public static String cleanBadChars(String string) {
		LOGGER.trace("cleanBadChars: string [{}]", string);
		/*
		 * Remove bad chars for the find function of bierdopje api.
		 */
		string = string.toLowerCase().replace(" and ", " & ");
		string = string.replace("&", "");
		string = string.replace("#", "");
		string = string.replace("*", "");
		string = string.replace("!", "");
		string = string.replace("$", "");
		string = string.replace("  ", " ");
		return string.trim();
	}

	public static void writeToFile(File file, Document doc) throws Exception {
		String xmlString = getXMLAsString(doc);
		try (FileOutputStream os = new FileOutputStream(file)) {
			byte[] xmlStringContent = xmlString.getBytes("UTF-8");
			os.write(xmlStringContent);
			os.close();
		}
	}

	public static String getXMLAsString(Document doc) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);

		return result.getWriter().toString();
	}

	public static String getXMLAsString(Element eElement) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");

		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(eElement);
		transformer.transform(source, result);

		return result.getWriter().toString();
	}

	public static Document getDocument(String string) throws ParserConfigurationException,
			SAXException, IOException {
		return getDocument(new ByteArrayInputStream(string.getBytes("UTF-8")));
	}

	public static Document getDocument(InputStream inputStream) throws ParserConfigurationException,
			IOException {
		Document doc;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// Use the factory to create a builder
		DocumentBuilder builder;
		builder = factory.newDocumentBuilder();
		try {
			doc = builder.parse(inputStream);
		} catch (SAXException e) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("getDocument: Not a valid XML document, setting a blank document!");
			} else {
				LOGGER.debug("Not a valid XML document, setting a blank document!");
			}
			doc = builder.newDocument();
		}

		return doc;
	}
}
