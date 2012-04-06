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

import edu.vu.isis.ammo.launch.constants.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import edu.vu.isis.ammo.INetPrefKeys;
import edu.vu.isis.ammo.api.AmmoPreference;

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
		
		//does a manual join.  the content provider probably won't let us do a real join.
		long rawContactId = getRawContactId(context, userId);
		if(rawContactId<0) {
			return null;
		}
		return getUnit(context, rawContactId);
	}
	
	
	
	private static long getRawContactId(Context context, String userId) {
		Cursor c = null;
		try {
			String selection = Data.MIMETYPE + "=? AND " + Data.DATA1 + "=?";
			String[] selectionArgs = { Constants.MIME_USERID, userId };

			c = context.getContentResolver().query(Data.CONTENT_URI, new String[]{ Data.RAW_CONTACT_ID }, selection, selectionArgs, null);
			if(c.getCount() < 1) {
				logger.error("Could not getRawContactId.  No records found with userId of " + userId);
				return -1;
			}
			c.moveToFirst();
			return c.getLong(0);
		}
		catch(Exception e) {
			logger.error("Could not getRawContactId: " + e, e);
			return -1;
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
	
	private static String getUnit(Context context, long rawContactId) {
		Cursor c = null;
		try {
			Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
			Uri dataUri = Uri.withAppendedPath(rawContactUri, RawContacts.Data.CONTENT_DIRECTORY);
			
			String selection = RawContacts.Data.MIMETYPE + "=?";
			String[] selectionArgs = { Constants.MIME_UNIT_NAME };
			
			c = context.getContentResolver().query(dataUri, new String[]{ RawContacts.Data.DATA1 }, selection, selectionArgs, null);
			if(c.getCount() < 1) {
				logger.error("Could not getUnit.  No records found with id of " + rawContactId);
				return null;
			}
			c.moveToFirst();
			return c.getString(0);
		}
		catch(Exception e) {
			logger.error("Could not getUnit: " + e, e);
			return null;
		}
		finally {
			if(c != null) {
				c.close();
			}
		}
	}
}
