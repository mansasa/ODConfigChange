package com.tesco.custom;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

public class CopyOfODConfiguration {

	private static final Logger log = Logger.getLogger(CopyOfODConfiguration.class.getName());
	private static final ResourceBundle requiredproperties = ResourceBundle
			.getBundle("config");
	private static final String pname = requiredproperties.getString("CONFIGPNAME");
	private static final String path = requiredproperties.getString("CONFIGPATH");
	private static final String odfile = requiredproperties.getString("CONFIGODFILE");
	private static final String stopcmd = requiredproperties.getString("CONFIGCMDSTOP");
	private static final String startcmd = requiredproperties.getString("CONFIGCMDSTART");
	private static final String bname = requiredproperties.getString("BASESERVER");
    private static final String ddfile = requiredproperties.getString("CONFIGDEPLOYFILE");

	public static void main(String argv[]) throws IOException,
			TransformerException, ParserConfigurationException, SAXException {

		configResartOD();
		odDeployandCopy();
		DDCopyUtil.copyDD(ddfile,
				"D:\\od-home\\OpenDeployNG\\conf\\GroceryContentSync.xml");

	}

	private static void odDeployandCopy() throws IOException,
			TransformerException, ParserConfigurationException, SAXException

	{

		log.info("#############################In OD Deployment odDeployandCopy Method ################################\n");

		// FileReader reader = new FileReader(pname);
		// Properties properties = new Properties();
		// properties.load(reader);
		// String[] servername =
		// properties.getProperty("Servername").split(",");

		// int numberofserver= servername.length;

		log.info("Modifying configuration file-" + ddfile + "\n");

		Document doc = openXMLFile(ddfile);

		setContent(doc, "localNode", "host", bname, 0);

		// setContent(doc, "nodeRef", "useNode", bname, 0);

		FileReader reader = new FileReader(pname);
		Properties properties = new Properties();
		properties.load(reader);
		String[] servername = properties.getProperty("Servername").split(",");
		for (String sname : servername) {

			addReceiver(sname, doc);

		}

		setContent(doc, "remoteDiff", "area", path, 0);

		setContent(doc, "targetFilesystem", "area", path, 0);

		writeToXML(ddfile, doc);

		log.info("#############################End of log file################################\n");

	}

	private static void setContent(Document doc, String node, String hostName,
			String baseSrvrName, int index) {

		Node localNode = doc.getElementsByTagName(node).item(index);
		NamedNodeMap attr = localNode.getAttributes();
		Node nodeAttr = attr.getNamedItem(hostName);
		nodeAttr.setTextContent(baseSrvrName);

	}

	private static void writeToXML(String filepath, Document doc)
			throws TransformerFactoryConfigurationError,
			TransformerException {
		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		// Pretty print XML
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		// End Pretty print XML
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(filepath));
		transformer.transform(source, result);
	}

	private static Document openXMLFile(String filepath)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		return docBuilder.parse(filepath);
	}

	private static void configResartOD()

	{
		int stopcount = 0, startcount = 0;
		List<String> failedstops = new ArrayList<>();
		List<String> failedstarts = new ArrayList<>();

		try {

			log.info("#############################Begning of log file################################\n");

			FileReader reader = new FileReader(pname);
			Properties properties = new Properties();
			properties.load(reader);
			String[] servername = properties.getProperty("Servername").split(
					",");

			for (String sname : servername) {

				log.info("-----------------------------------------------------------------\n");

				String filepath = "\\\\" + sname + odfile;
				log.info("Modifying configuration file-" + filepath + "\n");

				// Open XML File
				Document doc = openXMLFile(filepath);

				// Set the contents of the XML
				setContent(doc, "node", "host", bname, 1);

				setContent(doc, "path", "name", path, 3);

				// Write into the XML File
				writeToXML(filepath, doc);
				log.info("Modified configuration file\n");

				log.info("Stopping OpenDeploy Service on " + sname + "\n");
				Process p1 = Runtime.getRuntime().exec(
						"sc \\\\" + sname + " " + stopcmd);
				String stop = IOUtils.toString(p1.getInputStream());

				String stopsting = "STOP_PENDING";
				String runningstring = "RUNNING";

				Boolean stopflag, startflag;

				stopflag = stop.contains(stopsting);

				if (stopflag) {
					log.info(stop + "\n");
					log.info("Stopped OpenDeploy Service\n");
					log.info(stop + "\n");
				} else {
					stopcount++;
					log.info("Not able to stop service............\n");
					failedstops.add(sname);
				}
				log.info("Starting OpenDeploy Service on " + sname + "\n");
				Thread.sleep(5000); // 1000 milliseconds is one second.
				Process p2 = Runtime.getRuntime().exec(
						"sc \\\\" + sname + " " + startcmd);
				String start = IOUtils.toString(p2.getInputStream());

				startflag = start.contains(runningstring);
				if (startflag) {
					log.info(start + "\n");
					log.info("Started OpenDeploy Service\n");
				} else {
					startcount++;
					log.info("Not able to start service............\n");
					log.info(start + "\n");
					failedstarts.add(sname);
				}
				log.info("-----------------------------------------------------------------\n");

			}
			log.info("#############################End of log file################################\n");
			log.info("Total services failed to stop :" + stopcount + "\n");
			System.out.println("Total services failed to stop :" + stopcount);
			System.out
					.println("--------------------------------------------------------------");
			System.out.println("Failed service stop Server List :"
					+ failedstops);
			System.out
					.println("--------------------------------------------------------------");
			System.out.println("Total services failed to start :" + startcount);
			System.out
					.println("--------------------------------------------------------------");
			System.out.println("Failed service start Server List :"
					+ failedstarts);
			log.info("Total services failed to start :" + startcount + "\n");

		}

		catch (ParserConfigurationException | TransformerException | InterruptedException | SAXException | IOException pce) {

			log.error("Exception ::" + pce);
		}
	}
	private static void addReceiver(String server, Document doc)
			throws TransformerFactoryConfigurationError {
		Node replicationFarm = doc.getElementsByTagName("replicationFarm")
				.item(0);
		Element nodeRef = doc.createElement("nodeRef");
		Attr useNode = doc.createAttribute("useNode");
		useNode.setValue(server);
		nodeRef.setAttributeNode(useNode);
		replicationFarm.appendChild(nodeRef);

	}
}
