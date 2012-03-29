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

import android.content.Context;
import android.location.Location;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Subclass of TextView used to display location information in different formats.
 * 
 * Currently, Dash only displays location information in MGRS format, but it supports lat-lng 
 * format as well.
 * @author demetri
 *
 */
public class LocationTextView extends TextView {

	private Context mContext;
	private Location locationRef = null;
	
	public LocationTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	/**
	 * Takes lat lon coordinates and if settings dictate, transforms them to 
	 * MGRS and sets the view.
	 * @param lat
	 * @param lon
	 */
	public void setFormattedTextFromLocation(Location location) {
		super.setText(Util.toString(location, mContext));
	}
	
	public void notifyLocationChanged(Location location) {
		if (location != null) {
			this.setFormattedTextFromLocation(location);
			locationRef = location;	
		}
	}
	
	public boolean hasLocation() {
		return (locationRef != null);
	}
}
