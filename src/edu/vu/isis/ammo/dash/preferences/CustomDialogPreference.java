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
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import edu.vu.isis.ammo.dash.R;

public class CustomDialogPreference extends DialogPreference {

	// Don't want to see the dialog icon. Set it to null.
	public CustomDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDialogIcon(R.drawable.transparent_filler10x10);
	}

	public CustomDialogPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	// We don't want this view to be clickable. 
	@Override 
	public void onClick(DialogInterface di, int which)  {
		return;
	}
	
	@Override
	public void showDialog(Bundle state) {
//		if (this.isSelectable()) {
//			super.showDialog(state);
//		}
		
		return;
	}

	
	
}
