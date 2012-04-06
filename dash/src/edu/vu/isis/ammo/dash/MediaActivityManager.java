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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.MediaTableSchema;
import edu.vu.isis.ammo.dash.template.AmmoTemplateManagerActivity;

/**
 * See TigrMobile code for more information on the history of this class.
 * 
 * Like the class name indicates, this class is used to manage media created in Dash.
 * Most of these methods are convenience methods (hence the static scope) used throughout Dash
 * for different purposes such as writing data to the SD card and serializing data.
 */
public class MediaActivityManager {

	private static final int THUMBNAIL_HEIGHT = 480;
	
	private MediaActivityManager() {
	}
	
	private static final Logger logger = LoggerFactory.getLogger("class.MediaActivityManager");


	public static Uri createPhotoUri(Context context) {
		try {
			return Uri.fromFile(File.createTempFile(String.valueOf(System.currentTimeMillis()) + "-", ".jpg", Dash.IMAGE_DIR));
		}
		catch (IOException e) {
			logger.error("::createPhotoUri", e);
			return null;
		}
	}
	
	public static Intent createPictureIntent(Context context, String id, Uri photoUri) {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
		return intent;
	}
	
	public static Intent createAudioIntent(Context context, String id) {
		Intent intent = new Intent(context, AudioEntryActivity.class);
		intent.putExtra(AudioEntryActivity.INCIDENT_UUID, id);
		return intent;
	}
	
	public static Intent createTemplateIntent(Context context, String id, String template, String templateData, Location location) {
		return new Intent(context, AmmoTemplateManagerActivity.class)
		.putExtra(AmmoTemplateManagerActivity.TEMPLATE_EXTRA, template)
		.putExtra(AmmoTemplateManagerActivity.JSON_DATA_EXTRA, templateData)
		.putExtra(AmmoTemplateManagerActivity.OPEN_FOR_EDIT_EXTRA, true)
		.putExtra(AmmoTemplateManagerActivity.LOCATION_EXTRA, location);
	}
	
	public static Bitmap getThumbnail(Uri photoUri) {
		if (photoUri == null) {
			logger.error( "::getThumbnail - uri null");
			return null;
		}

		// Create a thumbnail from the full size image cached.
		Bitmap src = BitmapFactory.decodeFile(photoUri.getPath());
		
		if(src == null) {
			logger.error( "::getThumbnail - could not decode file: " + photoUri.getPath());
			return null;
		}
		
		// Scale width and height so height is 150px.
		int dstWidth = (int)Math.round((double)(src.getWidth()*THUMBNAIL_HEIGHT)/src.getHeight());
		int dstHeight = THUMBNAIL_HEIGHT;
		
		return Bitmap.createScaledBitmap(src, dstWidth, dstHeight, true);
	}
	/**
	 * Ideas from Dash class.
	 * @return true on success, false on failure.
	 */
	public static Uri processPicture(ContentResolver contentResolver, Intent data, String id, Bitmap thumbnail) {
		if (thumbnail == null) {
			logger.error( "::processPicture - uri null");
			return null;
		}

		// Write the thumbnail to the sd card and store the URI in our CP.
		String filePath = writeBitmapToSDCard(thumbnail, String.valueOf(System.currentTimeMillis()));
		if(filePath == null) {
			return null;
		}
		
		// Store the fileUri in the content provider.
		ContentValues cv = new ContentValues();
		cv.put(MediaTableSchema.EVENT_ID, id);
		cv.put(MediaTableSchema.DATA_TYPE, MediaTableSchema.IMAGE_DATA_TYPE);
		cv.put(MediaTableSchema.DATA, filePath);
		Uri uri = contentResolver.insert(MediaTableSchema.CONTENT_URI, cv);
		logger.debug( "Camera activity returned. Inserted " + filePath
				+ " into " + uri.toString());
		
		return uri;
	}
	
	private static String writeBitmapToSDCard(Bitmap bitmap, String filename) {
		FileOutputStream outStream = null;
		try {
			String exStoreState = Environment.getExternalStorageState();
			if (! exStoreState.equals(Environment.MEDIA_MOUNTED)) {
				logger.error("::writeBitmapToSDCard - no external storage");
				return null;
			}
			if (!Dash.CAMERA_DIR.exists()) {
				boolean success = Dash.CAMERA_DIR.mkdirs();
				if(!success) {
					logger.error("::writeBitmapToSDCard - error creating directories");
					return null;
				}
			}

			File file = new File(Dash.CAMERA_DIR, filename + ".jpg");
			outStream = new FileOutputStream(file);
			bitmap.compress(CompressFormat.JPEG, Dash.JPEG_QUALITY, outStream);

			return file.getAbsolutePath();

		} catch (FileNotFoundException e) {
			logger.error("::writeBitmapToSDCard -", e);
			return null;
		}
		finally {
			try {
				if(outStream != null) {
					outStream.close();
				}
			} catch (IOException e) {
				logger.error("::writeBitmapToSDCard -", e);
				e.printStackTrace();
			}
		}
	}
	
	public static Uri processAudio(ContentResolver contentResolver, Intent data, String id) {
		//does nothing because everything is handled by the AudioEntryDialog activity
		return data == null ? null : data.getData();
	}
	
	public static String processTemplate(ContentResolver contentResolver, Intent data, String id) {
		return data == null ? null : data.getStringExtra(AmmoTemplateManagerActivity.JSON_DATA_EXTRA);
	}

	public static Uri saveTemplateData(ContentResolver contentResolver, String id, String templateData) {
		// Store the text file in the sdcard and create a media entry.
		try {
			File dir = new File(Dash.TEMPLATE_DIR.getCanonicalPath()); 
			String filename = String.valueOf(System.currentTimeMillis());
			File currentFile = new File(dir, filename + "_template.txt");
			FileOutputStream fos = new FileOutputStream(currentFile);
			fos.write(templateData.getBytes());
			fos.close();

			// Insert media entry.
			ContentValues cv = new ContentValues();
			cv.put(MediaTableSchema.EVENT_ID, id);
			cv.put(MediaTableSchema.DATA_TYPE, MediaTableSchema.TEMPLATE_DATA_TYPE);
			cv.put(MediaTableSchema.DATA, currentFile.getCanonicalPath());
			Uri uri = contentResolver.insert(MediaTableSchema.CONTENT_URI, cv);
			logger.debug( "Inserted " + currentFile.getCanonicalPath() + " into " + uri.toString());
			return uri;

		} catch (IOException e) {
			logger.error("::saveTemplateData - ", e);
			return null;
		}
	}

	public static String getPath(ContentResolver contentResolver, Uri mediaUri) {
		try {
			Cursor cursor = contentResolver.query(mediaUri, new String[]{MediaTableSchema.DATA}, null, null, null);
			if(cursor.getCount()!=1) {
				logger.error("::getPath - media not found");
				return null;
			}
			cursor.moveToFirst();
			return cursor.getString(0);
		} 
		catch(Exception e) {
			//the API is pretty vague on the details of what exceptions Cursor will throw
			logger.error("::getPath - ", e);
			return null;
		}
	}
}
