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


import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;

/**
 * EditText widget that appears in a dialog when preference item is selected. 
 * @author demetri
 *
 */
public class MyEditTextPreference extends EditTextPreference {

	// ===========================================================
	// Constants
	// ===========================================================
	
	// ===========================================================
	// Fields
	// ===========================================================
	private String summaryPrefix = "";
	
	
	// ===========================================================
	// Lifecycle
	// ===========================================================
	public MyEditTextPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MyEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public MyEditTextPreference(Context context) {
		super(context);
	}
	
	public MyEditTextPreference(Context context, String aSummaryPrefix) {
		super(context);
		summaryPrefix = aSummaryPrefix;
	}
	
		
	
	@Override 
	protected void onDialogClosed (boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		
		// Set the summary field to newly set value in the edit text.
		this.refreshSummaryField();
	}
	
	/**
	 *  Set the summary field such that it displays the value of the edit text.
	 */
	public void refreshSummaryField() {
		this.setSummary(summaryPrefix + this.getText());	
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
}
