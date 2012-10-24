package liv.ac.uk.vomssnooper;


import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses an XML File of VOID cards from the CIC Portal
 * 
 * @author Steve Jones <sjones@hep.ph.liv.ac.uk>
 * @since 2012-02-24
 */

public class VoidCardXmlParser extends DefaultHandler {

	private VirtOrgInfo voInfo; // Single record of VO Info
	private ArrayList<VirtOrgInfo> voInfoList; // List of records of VO Info 

	private VomsServer vomsServer; // Record of a VOMS Server
	private IndividualContact contact; // Record of a contact of a VO
	private Fqan fqan ; // Fqan of VO

	private Boolean hasGlite; // Does this VO use gLite?
	
	private String xmlTag;    // Various parser state variables    
	private String xmlChars;
	private String xmlFile;

	/**
	 * Constructor
	 * 
	 * @param xmlFile XML File to parse
	 * @param voInfoList List of VOs
	 */
	public VoidCardXmlParser(String xmlFile, ArrayList<VirtOrgInfo> voInfoList) {
		this.xmlFile = xmlFile;
		this.voInfoList = voInfoList;
	}

	/**
	 * Parse the VOID XML file
	 * 
	 * @return null
	 */
	public void parseDocument() {

		// Set up SAX parser
		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {

			// Get a new instance of parser
			SAXParser parser = factory.newSAXParser();

			// Parse the XML, registering this class for call backs
			parser.parse(xmlFile, this);

			// Finally, sort those voms servers
			for (VirtOrgInfo v : voInfoList) {
				v.sortVomsServers();
			}
		}
		catch (SAXException e) {
			e.printStackTrace();
		}
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Event Handler for start elements
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (qName.equalsIgnoreCase("Middlewares")) {
			xmlTag = "Middlewares";

			// Does it have gLite?
			hasGlite = false;
			if (attributes.getValue("gLite").equals("1"))
				hasGlite = true;
		}

		if (qName.equalsIgnoreCase("IDCard")) {
			xmlTag = "IDCard";

			// Start a new VO record
			voInfo = new VirtOrgInfo();
			voInfo.setVoNameAndVoNickName(attributes.getValue("Name"));
			hasGlite = false;
		}

		if (qName.equalsIgnoreCase("VOMS_Server")) {
			xmlTag = "VOMS_Server";

			// Start a new VOMS Server record, and fill it
			vomsServer = new VomsServer();
			try {
				vomsServer.setHttpsPort(Integer.valueOf(attributes.getValue("HttpsPort")));
			}
			catch (NumberFormatException e) {
				System.out.print("Bad format for HttpsPort\n");
				vomsServer.setHttpsPort(-1);
			}
			try {
				vomsServer.setVomsServerPort(Integer.valueOf(attributes.getValue("VomsesPort")));
			}
			catch (NumberFormatException e) {
				System.out.print("Bad format for VomsesPort\n");
				vomsServer.setVomsServerPort(-1);
			}
			vomsServer.setHostname(attributes.getValue("hostname"));
			vomsServer.setMembersListUrl(attributes.getValue("MembersListUrl"));
		}

		if (qName.equalsIgnoreCase("Contact")) {
			xmlTag = "Contact";
			// Start a new contact record
			contact = new IndividualContact("dummy", "dummy", "dummy", "dummy");
		}
		
		if (qName.equalsIgnoreCase("FQAN")) {
			xmlTag = "FQAN";
			// Start a new FQAN record
			fqan = new Fqan ();
			fqan.setGroupType(attributes.getValue("GroupType"));
			String isUsed = attributes.getValue("IsGroupUsed");
			if (isUsed.equals("0")) {
				fqan.setIsUsed(false);
			}
			else {
				fqan.setIsUsed(true);
			}
		}

		if (qName.equals("X509Cert")) {
			xmlTag = "X509Cert";
		}
	}

	// Event Handler for end elements
	public void endElement(String uri, String localName, String qName) throws SAXException {

		if (qName.equalsIgnoreCase("IDCard")) {
			// Add gLite equipped VOs to the list
			if (hasGlite) {
				voInfoList.add(voInfo);
			}
		}

		// When we close Contact, add it to the list
		if (qName.equalsIgnoreCase("Contact")) {
			voInfo.addIc(contact);
			xmlTag = "";
		}
		
		// When we close FQAN , add it to the list
		if (qName.equalsIgnoreCase("FQAN")) {
			voInfo.addFqan(fqan);
			xmlTag = "";
		}
		
		// In FQAN data, transfer data and fix line breaks
		if (xmlTag.equals("FQAN")) {

			if (qName.equalsIgnoreCase("FqanExpr")) {
				fqan.setFqanExpr(xmlChars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("Description")) {
				if (fqan == null) {
				}
				fqan.setDescription(xmlChars.trim().replace("\n", ""));
			}
		}
		
		// If in Contact data, transfer data and fix line breaks
		if (xmlTag.equals("Contact")) {
			if (qName.equalsIgnoreCase("Name")) {
				contact.setName(xmlChars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("Role")) {
				contact.setRole(xmlChars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("Email")) {
				contact.setEmail(xmlChars.trim().replace("\n", ""));
			}
			if (qName.equalsIgnoreCase("DN")) {
				contact.setDn(xmlChars.trim().replace("\n", ""));
			}
		}

		// Add new VOMS servers
		if (qName.equalsIgnoreCase("VOMS_Server")) {
			// Throw away incomplete VOMS Servers
			if (vomsServer.getDn() != null) {
				voInfo.addVomsServer(vomsServer);
			}
		}

		// Set other attributes
		if (qName.equalsIgnoreCase("hostname")) {
			vomsServer.setHostname(xmlChars);
		}
		if (qName.equalsIgnoreCase("DN")) {
			if (xmlTag.equals("X509Cert")) {
				vomsServer.setDn(xmlChars);
			}
		}
		if (qName.equalsIgnoreCase("CA_DN")) {
			if (xmlTag.equals("X509Cert")) {
				vomsServer.setCaDn(xmlChars);
			}
		}

		// Finalise parser state
		if (qName.equalsIgnoreCase("Middlewares"))
			xmlTag = "";
		if (qName.equalsIgnoreCase("IDCard"))
			xmlTag = "";
		if (qName.equalsIgnoreCase("VOMS_Server"))
			xmlTag = "";
		if (qName.equalsIgnoreCase("X509Cert"))
			xmlTag = "";
	}

	// Capture interstitial characters
	public void characters(char[] ch, int start, int length) throws SAXException {
		xmlChars = new String(ch, start, length);
	}
}
