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
import edu.vu.isis.ammo.INetPrefKeys;
import edu.vu.isis.ammo.api.AmmoContacts;
import edu.vu.isis.ammo.api.AmmoPreference;
//import android.provider.ContactsContract.Data;
//import android.provider.ContactsContract.RawContacts;

/**
 * Convenience class used for accessing user information stored in the contacts tables.
 * @author demetri
 *
 */
public class ContactsUtil {
	private static final Logger logger = LoggerFactory.getLogger("class.ContactsUtil");
	
	private ContactsUtil() {}
	
	/**
	 * @return the unit for this current user or null if cannot be found.
	 */
	public static String getUnit(Context context) {
		String userId;
		try {
			userId = AmmoPreference
                .getInstance(context)
                .getString(INetPrefKeys.CORE_OPERATOR_ID, 
                           INetPrefKeys.DEFAULT_CORE_OPERATOR_ID);
		}
		catch(Exception e) {
			//for some reason the AmmoPreference system will sometimes throw exceptions
			logger.error("AmmoPreference threw an exception for something it probably should not have: " + e, e);
			return null;
		}
		if(userId == null) {
			logger.error("AmmoPreference:  No operator id");
			return null;
		}
		

		AmmoContacts mData = AmmoContacts.newInstance(context);
		AmmoContacts.Contact g = mData.getContactByUserId(userId);
		if (g == null) return null;
		
		String unit = g.getUnit();
		return unit;
		
	}
	
}
