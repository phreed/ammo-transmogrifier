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
package edu.vu.isis.ammo.dash.template.view;

import android.view.View;

/**
 * Interface that should be implemented by all template widgets so proper data
 * may be acquired by interested parties.
 * 
 * @author adrian
 * 
 */
public interface GuiField {
	/**
	 * @return the View representing this field
	 */
	View getView();

	/**
	 * @return an ID that will be used to uniquely identify this GuiField. This
	 *         will typically come from an id attribute inside the XML element
	 *         that this field was created from.
	 */
	String getId();

	/**
	 * @return the XML tag that is used to trigger creation of this GuiField
	 */
	String getTag();

	/**
	 * @return the label for this GuiField
	 */
	String getLabel();

	/**
	 * @return the value bound to this GuiField.
	 */
	String getValue();

	/**
	 * If we are set to read only mode, then set the TextView to the value of
	 * the String given. Otherwise, parse the String to set the GuiField to have
	 * the appropriate state.
	 * 
	 * @param value
	 *            the value to which this GuiField will be set
	 */
	void setValue(String value);

	/**
	 * This replaces the field with a TextView.
	 */
	void makeReadOnly();
}
