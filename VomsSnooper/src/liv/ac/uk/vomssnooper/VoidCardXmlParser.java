package liv.ac.uk.vomssnooper;

import java.io.IOException;
import java.util.ArrayList;
// import java.util.Iterator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses an XML File of VOID cards
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-02-24
 */
public class VoidCardXmlParser extends DefaultHandler {

	private VirtOrgInfo currentVoInfo;
	private VomsServer currentVomsServer;
	private IndividualContact currentIc;

	private Boolean hasGlite;
	private String currentTag;
	private String chars;
	private String xmlFile;
	private ArrayList<VirtOrgInfo> allVoidInfo;

	/**
	 * Constructor
	 * 
	 * @param x XML FIle to parse
	 * @param v Where to parse the results into
	 */
	public VoidCardXmlParser(String x, ArrayList<VirtOrgInfo> v) {
		xmlFile = x;
		allVoidInfo = v;
	}

	/**
	 * Parse the VOID XML file
	 * 
	 * @return null
	 */
	public void parseDocument() {

		// Set up SAX Parser
		SAXParserFactory spf = SAXParserFactory.newInstance();
		try {

			// get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			// parse the file and also register this class for call backs
			sp.parse(xmlFile, this);

			// Finally, sort those voms servers

			for (VirtOrgInfo voi : allVoidInfo) {
				voi.sortVomsServers();
			}
		}
		catch (SAXException se) {
			se.printStackTrace();
		}
		catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		}
		catch (IOException ie) {
			ie.printStackTrace();
		}
	}

	// Event Handlers
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("Middlewares")) {
			currentTag = "Middlewares";
			hasGlite = false;
			if (attributes.getValue("gLite").equals("1"))
				hasGlite = true;
		}

		if (qName.equalsIgnoreCase("IDCard")) {
			currentTag = "IDCard";

			hasGlite = false;

			currentVoInfo = new VirtOrgInfo();
			currentVoInfo.setVoNameAndVoNickName(attributes.getValue("Name"));
		}

		if (qName.equalsIgnoreCase("VOMS_Server")) {
			currentTag = "VOMS_Server";
			currentVomsServer = new VomsServer();
			try {

				currentVomsServer.setHttpsPort(Integer.valueOf(attributes.getValue("HttpsPort")));
			}
			catch (NumberFormatException e) {
				System.out.print("Bad format for port, HttpsPort\n");
				currentVomsServer.setHttpsPort(-1);
			}
			try {

				currentVomsServer.setVomsServerPort(Integer.valueOf(attributes.getValue("VomsesPort")));
			}
			catch (NumberFormatException e) {
				System.out.print("Bad format for port, VomsesPort\n");
				currentVomsServer.setVomsServerPort(-1);
			}

			currentVomsServer.setHostname(attributes.getValue("hostname"));
			currentVomsServer.setMembersListUrl(attributes.getValue("MembersListUrl"));

		}

		if (qName.equalsIgnoreCase("Contact")) {
			currentTag = "Contact";
			currentIc = new IndividualContact("nowt", "nowt", "nowt", "nowt");
		}

		if (qName.equals("X509Cert")) {
			currentTag = "X509Cert";
		}

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (qName.equalsIgnoreCase("IDCard")) {
			if (hasGlite) {
				allVoidInfo.add(currentVoInfo);
			}
		}
		if (qName.equalsIgnoreCase("Contact")) {
			currentVoInfo.addIc(currentIc);
			currentTag = "";
		}

		if (currentTag.equals("Contact")) {
			if (qName.equalsIgnoreCase("Name")) {

				currentIc.setName(chars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("Role")) {
				currentIc.setRole(chars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("Email")) {
				currentIc.setEmail(chars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("DN")) {
				currentIc.setDn(chars.trim().replace("\n", ""));
			}
		}

		if (qName.equalsIgnoreCase("VOMS_Server")) {
			// Throw away incomplete VOMS Servers
			if (currentVomsServer.getDn() != null) {
				currentVoInfo.addVomsServer(currentVomsServer);
			}
		}
		if (qName.equalsIgnoreCase("hostname")) {
			currentVomsServer.setHostname(chars);
		}
		if (qName.equalsIgnoreCase("DN")) {
			if (currentTag.equals("X509Cert")) {
				currentVomsServer.setDn(chars);
			}
		}
		if (qName.equalsIgnoreCase("CA_DN")) {
			if (currentTag.equals("X509Cert")) {
				currentVomsServer.setCaDn(chars);
			}
		}
		if (qName.equalsIgnoreCase("Middlewares"))
			currentTag = "";
		if (qName.equalsIgnoreCase("IDCard"))
			currentTag = "";
		if (qName.equalsIgnoreCase("VOMS_Server"))
			currentTag = "";
		if (qName.equalsIgnoreCase("X509Cert"))
			currentTag = "";
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		chars = new String(ch, start, length);
	}

}
