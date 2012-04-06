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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.widget.Toast;
import edu.vu.isis.ammo.dash.preferences.DashPreferences;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.MediaTableSchema;
import edu.vu.isis.ammo.util.CoordinateConversion;

/**
 * Set of utility methods used throughout Dash for tasks like location conversions and file reading/manipulation.
 * @author demetri
 * @author adrian
 *
 */
public class Util {
	private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM-dd-yyyy", Locale.US);
	private static final Logger logger = LoggerFactory.getLogger("class.Util");
	
	
	private Util() {}
	
	public static String toMGRSString(Location location) {
		return location==null ? "" : new CoordinateConversion().latLon2MGRUTM(location.getLatitude(), location.getLongitude());
	}
	
	public static Location toLocation(String mgrs) {
		if(mgrs == null) {
			return null;
		}
		
		try {
			double[] location = new CoordinateConversion().mgrutm2LatLon(mgrs);
			return buildLocation(location[0], location[1]);
		}
		catch(Exception exception) {
			//wow, this code just throws random exceptions if the input is invalid
			return null;
		}
	}

	public static Location buildLocation(double latitude, double longitude) {
		Location location = new Location(LocationManager.GPS_PROVIDER);
		location.setLatitude(latitude);
		location.setLongitude(longitude);
		return location;
	}

	public static String toString(Location location, Context context) {
		if(DashPreferences.isMGRSPreference(context)) {
			return toMGRSString(location);
		}
		return location.getLatitude() + ", " + location.getLongitude();
	}

	public static String formatTime(long time) {
		return dateFormat.format(time);
	}

	/**
	 * Create files for collector
	 * 
	 * @throws IOException
	 */
	public static void setupFilePaths() throws IOException {
		String state = android.os.Environment.getExternalStorageState();
		if (!state.equals(android.os.Environment.MEDIA_MOUNTED)) {
			throw new IOException("SD Card is not mounted.  It is " + state + ".");
		}

		if (! createPath(Dash.CAMERA_DIR)) 
			throw new IOException("error with camera directory :"+ Dash.CAMERA_DIR);
		if (! createPath(Dash.IMAGE_DIR)) 
			throw new IOException("error with image directory :"+ Dash.IMAGE_DIR);
		if (! createPath(Dash.AUDIO_DIR)) 
			throw new IOException("error with audio directory :"+ Dash.AUDIO_DIR);
		if (! createPath(Dash.TEXT_DIR))
			throw new IOException("error with text directory :"+ Dash.TEXT_DIR);
		if (! createPath(Dash.VIDEO_DIR)) 
			throw new IOException("error with video directory :"+ Dash.VIDEO_DIR);
		if (! createPath(Dash.TEMPLATE_DIR)) 
			throw new IOException("error with template directory :"+ Dash.TEMPLATE_DIR);
	}

	private static boolean createPath(File directory) {
		try {
			if (directory.exists()) return true;
			if (directory.mkdirs()) return true;
			logger.error("could not create paths {}", directory.toString());
			Util.drillDirectory(directory);
			
		} catch (SecurityException ex) {
			logger.error("security exception {}",ex.getLocalizedMessage());
		}
		return false;
	}
	private static int drillDirectory(File dir) {
		if (dir == null) return 0;
		int level = Util.drillDirectory(dir.getParentFile()) + 1; // print parents first
		
		if (dir.exists()) {
			logger.error("l:{} {} read:{} write:{}",
					new Object[]{level, dir.toString(), dir.canRead(), dir.canWrite()} );
		}		
		return level;
	}
	
	public static void makeToast(Context context, String string) {
		Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
		logger.info(string);
	}
	
	
	
	/**
	 * @return bytes
	 */
	public static long getSize(long baseDashSize, Context context, Uri currentMediaUri, String templateData) {
		return baseDashSize +
			getSize(context.getContentResolver(), currentMediaUri) +
			getSize(templateData);
	}
	
	public static int getMediaCount(Uri currentMediaUri, String templateData) {
		int mediaCount = 0;
		if (currentMediaUri != null) {
			mediaCount++;
		}
		
		if (templateData != null) {
			mediaCount++;
		}
		return mediaCount;
	}
	
	private static int getSize(String data) {
		return data == null ? 0 : data.getBytes().length;
	}
	
	/**
	 * @return bytes for an object inserted into the ContentResolver with a DATA object to a path on the system, or 0 on error.
	 */
	private static long getSize(ContentResolver contentResolver, Uri uri) {
		if(uri == null) {
			return 0;
		}
		String filePath = getString(contentResolver, uri, MediaTableSchema.DATA);
		if(filePath == null) {
			return 0;
		}
		File file = new File(filePath);
		if(!file.exists()) {
			logger.error("::getSize: File does not exist: " + file);
			return 0;
		}
		
		return file.length();
	}

	private static String getString(ContentResolver contentResolver, Uri uri, String field) {
		if(uri == null || field == null) {
			logger.error("::getString: No uri or field.");
			return null;
		}
		Cursor cursor = null;
		try {
			cursor = contentResolver.query(uri, new String[]{field}, null, null, null);
			if(cursor == null || cursor.moveToFirst() == false || cursor.getColumnIndex(field) == -1) {
				logger.error("::getString: No data for uri/field: " + uri + ", " + field);
				return null;
			}
			
			String string = cursor.getString(cursor.getColumnIndex(field));
			if(string == null) {
				logger.error("::getString: No data at uri/field: " + uri + ", " + field);
				return null;
			}
			return string;
		}
		catch(Exception e) {
			logger.error("::getString: No data at uri/field: " + uri + ", " + field, e);
			return null;
		}
		finally {
			if(cursor != null) {
				cursor.close();
			}
		}
	}
}
