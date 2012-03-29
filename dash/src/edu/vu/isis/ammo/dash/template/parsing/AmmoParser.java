/*Copyright (C) 2010-2012 Institute for Software Integrated Systems (ISIS)
This software was developed by the Institute for Software Integrated
Systems (ISIS) at Vanderbilt University, Tennessee, USA for the 
Transformative Apps program under DARPA, Contract # HR011-10-C-0175.
The United States Government has unlimited rights to this software. 
The US government has the right to use, modify, reproduce, release, 
perform, display, or disclose computer software or computer software 
documentation in whole or in part, in any manner and for any 
purpose whatsoever, and to have or authorize others to do so.
*/
package edu.vu.isis.ammo.dash.template.parsing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.ViewGroup;
import edu.vu.isis.ammo.dash.preferences.ContactsUtil;
import edu.vu.isis.ammo.dash.template.view.FreeformTextView;
import edu.vu.isis.ammo.dash.template.view.GuiField;
import edu.vu.isis.ammo.dash.template.view.LocationView;
import edu.vu.isis.ammo.dash.template.view.PulldownView;

/**
 * Class that handles parsing whatever xml file is provided by the template
 * manager.
 * 
 * XML documents are parsed using a DOM parser.
 * 
 * This class uses a recursive parsing algorithm (technically it doesn't recurse right now, but the code
 * is set up in such a way that it can be if needed) to read the template xml files. Certain 
 * attributes supported for different widgets are defined below.
 * 
 * Questions about how the parsing works should be directed to Alan (if he's still working here) or Fred.
 * 
 * @author Demetri Miller
 * 
 */
public class AmmoParser {

	public static final String ROOT_TAG = "template";
	public static final String TEXT_TAG = "text";
	public static final String PULLDOWN_TAG = "pulldown";
	public static final String LOCATION_TAG = "location";
	
	public static final String ID_FIELD = "id";
	public static final String LABEL_FIELD = "label";
	public static final String HINT_FIELD = "hint";
	public static final String TEXT_FIELD = "text";
	public static final String PREPOP_FIELD = "prepop";	// If a field should be prepopulated with some value.
	
	private static final String PREPOP_TIME_FORMATTED = "time_formatted";
	private static final String PREPOP_TIME_UTC = "time_utc";
	private static final String PREPOP_UNIT = "unit";
	
	private static Map<String, String> prepopMap = new HashMap<String, String>();
	
	/**
	 * Parses the xml file and returns a map of ui elements.
	 * @param activity
	 * @param file
	 * @param rootView
	 * @param templateDisplayName
	 * @param location
	 * @return
	 */
	public static Map<String, GuiField> parseXMLForFileIntoView(Activity activity, File file, ViewGroup rootView, StringBuilder templateDisplayName, Location location) {
		Document document = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(new FileInputStream(file));
			NodeList list = document.getElementsByTagName(ROOT_TAG);

			setupPrepop(activity);
			
			if(templateDisplayName != null) {
				// Get the first element and set the report label.
				Element eNode = (Element)list.item(0);
				String label = eNode.getAttribute(LABEL_FIELD);
				templateDisplayName.append(label);
			}

			return AmmoParser.traverse(activity, document.getFirstChild(), rootView, location);
			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/**
	 * Look at each node in the xml file, determine the type, and initialize so it may be added to the TemplateView later on.
	 * 
	 * @param activity
	 * @param node
	 * @param rootView
	 * @param location
	 * @return
	 */
	public static Map<String, GuiField> traverse(Activity activity, Node node, ViewGroup rootView, Location location) {
		int childCount = node.getChildNodes().getLength();
		NodeList children = node.getChildNodes();
		Map<String, GuiField> fields = new HashMap<String, GuiField>();
		
		for (int i=0; i<childCount; i++) {
			if (children.item(i).getNodeType() != Node.ELEMENT_NODE) continue;
			
			Element eNode = (Element)children.item(i);
			String nodeName = eNode.getTagName();
			
			if (nodeName.equalsIgnoreCase(ROOT_TAG)) {
				return AmmoParser.traverse(activity, eNode.getFirstChild(), rootView, location);
			} else if (nodeName.equalsIgnoreCase(TEXT_TAG)) {
				GuiField field = new FreeformTextView(activity, eNode);
				fields.put(field.getId(), field);
				rootView.addView(field.getView());
			} else if (nodeName.equalsIgnoreCase(PULLDOWN_TAG)) {
				GuiField field = new PulldownView(activity, eNode);
				fields.put(field.getId(), field);
				rootView.addView(field.getView());
			} else if (nodeName.equalsIgnoreCase(LOCATION_TAG))	{
				LocationView field = new LocationView(activity, eNode);
				field.setLocation(location);
				fields.put(field.getId(), field);
				rootView.addView(field.getView());
			}
		}
		
		return fields;
	}
	
	/**
	 * Add prepopulated values to a map that can be used by widgets if they need to prepopulate
	 * their contents.
	 * @param context
	 */
	private static void setupPrepop(Context context) {
		Date time = new Date();
		prepopMap.put(PREPOP_TIME_FORMATTED, time.toGMTString());
		prepopMap.put(PREPOP_TIME_UTC, String.valueOf(time.getTime()));
		prepopMap.put(PREPOP_UNIT, ContactsUtil.getUnit(context));
	}
	
	public static Map<String, String> getPrepopMap() {
		return prepopMap;
	}
}
