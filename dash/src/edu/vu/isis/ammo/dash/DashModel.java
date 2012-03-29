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

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import edu.vu.isis.ammo.dash.preferences.ContactsUtil;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.EventTableSchema;

/**
 * Model object used as a wrapper for all data associated with a Dash report.
 * @author demetri
 *
 */
public class DashModel {
	private ContentValues model = new ContentValues();
	private Context context;
	
	private Bitmap thumbnail;
	private Uri photoUri;
	private Uri currentMediaUri;
	private int currentMediaType;
	private String templateData;
	
	
	private static final long BASE_DASH_SIZE = 20;
	

	public DashModel(Context context) {
		this.context = context;
	}
	
	public ContentValues getContentValues() {
		setAdditionalFields();
		return model;
	}
	
	public void setContentValues(ContentValues model) {
		this.model = model;
		if(this.model == null) {
			this.model = new ContentValues();
		}
	}
	
	public String getId() {
		return model.getAsString(EventTableSchema.UUID);
	}
	
	public void setId(String id) {
		model.put(EventTableSchema.UUID, id);
	}
	
	public String getOriginator() {
		return model.getAsString(EventTableSchema.ORIGINATOR);
	}
	
	public void setOriginator(String originator) {
		model.put(EventTableSchema.ORIGINATOR, originator);
	}
	
	public String getDescription() {
		return model.getAsString(EventTableSchema.DESCRIPTION);
	}

	public void setDescription(String description) {
		model.put(EventTableSchema.DESCRIPTION, description);
	}
	
	public Long getTime() {
		return model.getAsLong(EventTableSchema.MODIFIED_DATE);
	}
	
	public void setTime(Long time) {
		model.put(EventTableSchema.CREATED_DATE, time);
		model.put(EventTableSchema.MODIFIED_DATE, time);
	}
	
	public Location getLocation() {
		if(model.containsKey(EventTableSchema.LATITUDE) && model.containsKey(EventTableSchema.LONGITUDE)) {
			return Util.buildLocation(model.getAsDouble(EventTableSchema.LATITUDE), model.getAsDouble(EventTableSchema.LONGITUDE));
		}
		return null;
	}
	
	public void setLocation(Location location) {
		if(location != null) {
			model.put(EventTableSchema.LATITUDE, location.getLatitude());
			model.put(EventTableSchema.LONGITUDE, location.getLongitude());
		}
	}
	
	public Uri getCurrentMediaUri() {
		return currentMediaUri;
	}

	public void setCurrentMediaUri(Uri currentMediaUri) {
		this.currentMediaUri = currentMediaUri;
	}

	public int getCurrentMediaType() {
		return currentMediaType;
	}

	public void setCurrentMediaType(int currentMediaType) {
		this.currentMediaType = currentMediaType;
	}
	
	public Uri getPhotoUri() {
		return photoUri;
	}

	public void setPhotoUri(Uri photoUri) {
		this.photoUri = photoUri;
	}

	public Bitmap getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(Bitmap thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getTemplateData() {
		return templateData;
	}

	public void setTemplateData(String templateData) {
		this.templateData = templateData;
	}
	
	//
	// Code that massages the data model, to prepare it for delivery:
	//

	private void setAdditionalFields() {
		model.put(EventTableSchema.UNIT, ContactsUtil.getUnit(context));
		model.put(EventTableSchema.STATUS, EventTableSchema.STATUS_SENT);
		
		//For a "dash", these are intentionally blank
		model.put(EventTableSchema.CATEGORY_ID, "");
		model.put(EventTableSchema.DEST_GROUP_TYPE, "");
		model.put(EventTableSchema.DEST_GROUP_NAME, "");
		model.put(EventTableSchema.TITLE, "");
		model.put(EventTableSchema.DISPLAY_NAME, "<no title>");
		
		model.put(EventTableSchema.MEDIA_COUNT, Util.getMediaCount(currentMediaUri, templateData));
		model.put(EventTableSchema.SIZE, Util.getSize(BASE_DASH_SIZE, context, currentMediaUri, templateData)/1000.);
	}

}
