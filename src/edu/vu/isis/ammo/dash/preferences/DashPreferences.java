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
import android.os.Bundle;
import edu.vu.isis.ammo.dash.R;

/**
 * Preference activity used to display certain preferences (well, duh). Mainly the
 * version number of Dash.
 * 
 * This class is currently not in use. 
 * @author demetri
 *
 */
public class DashPreferences extends PreferenceActivityEx {
	
	// ===========================================================
	// Constants
	// ===========================================================
	public static final String PREF_DASH_LIMIT = "DASH_RETRIEVAL_LIMIT";
	public static final String DEFAULT_PREF_DASH_LIMIT = "50";
	
	// ===========================================================
	// Fields
	// ===========================================================
	private MyEditTextPreference dashLimit;
	
	// ===========================================================
	// Lifecycle
	// ===========================================================
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.addPreferencesFromResource(R.xml.dash_preferences);
		dashLimit = (MyEditTextPreference) findPreference(PREF_DASH_LIMIT);
		
		this.setupViews();
	}

	// ===========================================================
	// UI Management
	// ===========================================================
	public void setupViews() {
		// Set the summary of each edit text to the current value
		// of its EditText field.
		if (dashLimit != null) dashLimit.refreshSummaryField();
	}

	public static boolean isMGRSPreference(Context context) {
		return true;
	}

	// ===========================================================
	// Methods
	// ===========================================================
	
}
