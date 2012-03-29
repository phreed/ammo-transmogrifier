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
package edu.vu.isis.ammo.dash.template.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Model class used to hold information associated with a template.
 * @author demetri
 *
 */
public class Record {
	private String templateDisplayName;
	private Map<String, String>fields = new HashMap<String, String>();
	
	public String getField(String id) {
		return fields.get(id);
	}
	
	public void setField(String id, String field) {
		fields.put(id, field);
	}
	
	public void setTemplateDisplayName(String templateDisplayName) {
		this.templateDisplayName = templateDisplayName;
	}
	
	public String getTemplateDisplayName() {
		return templateDisplayName;
	}
}
