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
package edu.vu.isis.ammo.dash;


/**
 * @author phreed
 *
 */
public interface IDash {
	public static final String VIDEO_INTENT = "edu.vu.isis.ammo.dash.videoactivity.LAUNCH";
	public static final String VIDEO_PREVIEW_INTENT = "edu.vu.isis.ammo.dash.videopreviewactivity.LAUNCH";
	public static final String SETTINGS_INTENT = "edu.vu.isis.ammo.dash.preferences.DashPreferences.LAUNCH";
	public static final String BROWSE_REPORTS_INTENT = "edu.vu.isis.ammo.dash.ReportBrowserActivity.LAUNCH";
	public static final String STOP_PROGRESS_DIALOG_INTENT = "edu.vu.isis.ammo.dash.STOP_PROGRESS_DIALOG";
	public static final String VIEW_SUBSCRIPTIONS_INTENT = "edu.vu.isis.ammo.dash.SubscriptionViewer.LAUNCH";

	public static final String EXTRA_INCIDENT_ID = "extra_incident_id";
	public static final String EXTRA_INCIDENT_UUID = "extra_incident_uuid";

	public static final String MIME_TYPE_EXTENSION_BROADCAST = "broadcast";
	public static final String MIME_TYPE_EXTENSION_CALLSIGN = "callsign";
	public static final String MIME_TYPE_EXTENSION_TIGR_UID = "tigr_uid";
	public static final String MIME_TYPE_EXTENSION_UNIT = "unit";
}


