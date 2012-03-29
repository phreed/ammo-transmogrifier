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
package edu.vu.isis.ammo.dash.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
/**
 * This is a checkbox widget that can be used in the preferences screen of Dash. 
 * It currently is not in use.
 * @author demetri
 *
 */
public class CustomCheckBoxPreference extends CheckBoxPreference {

	// ===========================================================
	// Constants
	// ===========================================================
	public static final Logger logger = LoggerFactory.getLogger(CustomCheckBoxPreference.class);
	
	public static enum Type {
		JOURNAL
	};
	// ===========================================================
	// Fields
	// ===========================================================
	private String summaryPrefix = "";
	private Type mType;
	
	// ===========================================================
	// Lifecycle
	// ===========================================================
	public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CustomCheckBoxPreference(Context context) {
		super(context);
	}
	
	public CustomCheckBoxPreference(Context context, String aSummaryPrefix) {
		super(context);
		summaryPrefix = aSummaryPrefix;
	}
	
	// ===========================================================
	// 
	// ===========================================================

	/**
	 *  Set the summary field such that it displays the value of the edit text.
	 */
	public void refreshSummaryField() {
		if (!summaryPrefix.equals("")) {
			this.setSummary(summaryPrefix + this.isChecked());	
		}
	}
	
	// ===========================================================
	// Getters/Setters Methods
	// ===========================================================
	public String getSummaryPrefix() {
		return summaryPrefix;
	}

	public void setSummaryPrefix(String summaryPrefix) {
		this.summaryPrefix = summaryPrefix;
	}

	public void setType(Type mType) {
		this.mType = mType;
	}

	public Type getType() {
		return mType;
	}
	
	
}
