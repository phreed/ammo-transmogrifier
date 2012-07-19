package edu.vu.isis.ammo.dash.template.parsing;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class containing static helper functions for parsing template views
 * @author Nick King
 */
public class ViewParsers {

	private ViewParsers() {}
	
	public static List<String> getTextFromChildNodes(Element eNode) {
		NodeList children = eNode.getChildNodes();
		List<String> values = new ArrayList<String>();

		for (int i=0; i<children.getLength(); i++) {
			if (children.item(i).getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
		
			Element child = (Element)children.item(i);
			if (child.getChildNodes().item(0).getNodeType() != Node.TEXT_NODE) {
				continue;
			}
			
			String text = child.getChildNodes().item(0).getNodeValue();
			values.add(text);
		}
		
		return values;
	}
}
