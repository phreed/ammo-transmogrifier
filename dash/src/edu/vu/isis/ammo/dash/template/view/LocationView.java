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
package edu.vu.isis.ammo.dash.template.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import edu.vu.isis.ammo.dash.Dash;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.Util;
import edu.vu.isis.ammo.dash.WorkflowLogger;
import edu.vu.isis.ammo.dash.template.AmmoTemplateManagerActivity;
import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;

/** 
 * Contains an EditText to hold the location and map picker button to load it from a separate
 * map application.
 * @author adrian
 * @author demetri
 *
 */
public class LocationView implements GuiField {
	// =====================
	// Constants
	// =====================
    private static final Logger logger = LoggerFactory.getLogger("class.LocationView");
	
	private static final double DEFAULT_MAP_SCALE = 15;
	private static final String MAP_PICKER_ACTIVITY = "android.intent.action.POINT_SELECT";
	private static final String ZOOM_LEVEL_EXTRA = "zoomLevel";
	private static final String LAT_EXTRA = "LAT";
	private static final String LON_EXTRA = "LON";
	
	

	// =====================
	// Fields
	// =====================
	private ViewGroup viewGroup;
	private Activity activity;
	private TextView editText;
	private String id, labelValue, textValue, hintValue;
//	private String tag;
	private LocationManager locMgr;
	private Location location;
	// =====================
	// Lifecycle/Location Mgm
	// =====================
	/**
	 * @param activity the activity in which this widget is used
	 * @param eNode if this is null, then we will try to create you a normal widget.
	 */
	public LocationView(Activity activity, Element eNode) {
		this.activity = activity;
		if(eNode == null) {
			//setup a normal widget
			viewGroup = (ViewGroup)activity.getWindow().getDecorView();
		}
		else {
			// Inflate location edit text and set up location manager. Hook them together.
			viewGroup = (ViewGroup)LayoutInflater.from(activity).inflate(R.layout.template_location_view, null);
//			tag = AmmoParser.LOCATION_TAG;
		}
		
		// Set all our view references here.
		editText = (TextView)viewGroup.findViewById(R.id.location_view_field);
		
		// Parse the xml file if needed. This parsing needs to occur before the location services
		// are started since it will initialize our fields to default values.
		// If we're not loading the view as a template, just set the textfield to the default MGRS.
		if (eNode != null) {
			parseXml(eNode);
		} else {
			editText.setText("31NAA6602100000");	
		}
		
		ImageButton mapPickerButton = (ImageButton)viewGroup.findViewById(R.id.location_view_map_picker_button);
		//mapPickerButton.setVisibility(View.GONE);
		mapPickerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchMapPickerActivity();
			}
		});
		
		
		// Set up location manager.
		this.startLocationService(activity);
	}
	
	private void parseXml(Element eNode) {
		if(eNode == null) {
			return;
		}
		
		TextView label = (TextView) viewGroup.findViewById(R.id.location_view_label);
		id = eNode.getAttribute(AmmoParser.ID_FIELD);
		labelValue = eNode.getAttribute(AmmoParser.LABEL_FIELD);
		if(labelValue == null || labelValue.length() == 0) {
			labelValue = id;
		}
		label.setText(labelValue + ":");
		
		textValue = eNode.getAttribute(AmmoParser.TEXT_FIELD);
		if (textValue != null && textValue.length() != 0) {
			editText.setText(textValue);
		}
		
		hintValue = eNode.getAttribute(AmmoParser.HINT_FIELD);
		if (hintValue != null && hintValue.length() != 0) {
			editText.setHint(hintValue);
		}
	}
	
	private LocationListener onLocationChange = new LocationListener() {
		public void onLocationChanged(Location location) {
			setLocationFromGPS(location);
		}

		public void onProviderDisabled(String provider) {}

		public void onProviderEnabled(String provider) {}

		public void onStatusChanged(String provider, int status, Bundle extras) {}
	};

	private void startLocationService(Context context) {
		locMgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if (locMgr == null) {
			logger.error("Could not acquire LocationManager service. Location information will not be available for this application lifecycle.");
			return;
		}
		
		resume();
		
		if (locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null) {
			setLocationFromGPS(locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		}
	}
	
	// ========================================
	// Fields
	// ========================================
	private void setLocationFromGPS(Location location) {
		if(this.location == null) {
			//only set GPS point if no point has been set
			setLocation(location);
		}
	}
	
	public void setLocation(Location location) {
		// Only set the location if it isn't null.
		if (location != null) {
			this.location = location;
			editText.setText(Util.toMGRSString(location));
			WorkflowLogger.log("LocationView - setLocation with lat: " + location.getLatitude() + " long:" + location.getLongitude());
		}
	}
	
	// ========================================
	// Map picker
	// ========================================
	
	private void launchMapPickerActivity() {
		// FIXME: Temporarily short-circuit the map picker since it's broken
		// on the atterasys side.
//		if (true) {
//			Util.makeToast(activity, "Map picker currently not available");
//			return;
//		}
			
		Intent intent = new Intent(MAP_PICKER_ACTIVITY);
		double mapScale = 2;
		Location mapLocation = new Location(LocationManager.GPS_PROVIDER);
		if(location != null) {
			mapScale = DEFAULT_MAP_SCALE;
			mapLocation = location;
		}
		
		intent.putExtra(ZOOM_LEVEL_EXTRA, mapScale);
		intent.putExtra(LAT_EXTRA, mapLocation.getLatitude());
		intent.putExtra(LON_EXTRA, mapLocation.getLongitude());
		intent.putExtra(AmmoTemplateManagerActivity.LOCATION_FIELD_ID_EXTRA, getId());
		
		try {
			WorkflowLogger.log("LocationView - starting MapPicker activity");
			activity.startActivityForResult(intent, Dash.MAP_TYPE);
		}
		catch(ActivityNotFoundException e) {
			logger.error("::launchMapPickerActivity: no " + MAP_PICKER_ACTIVITY + " activity found.", e);
			Util.makeToast(activity, "Could not launch map picker.");
		}
	}
	
	public boolean processMapPoint(Intent data) {
		if(data == null) {
			logger.error("no data");
			Util.makeToast(activity, "Could not get map point");
			return false;
		}
		
		if(!data.hasExtra(LAT_EXTRA) || !data.hasExtra(LON_EXTRA)) {
			logger.error("no lat or lon");
			Util.makeToast(activity, "Could not get map point");
			return false;
		}
		
		Location mapLocation = new Location(LocationManager.GPS_PROVIDER);
		mapLocation.setLatitude(data.getDoubleExtra(LAT_EXTRA, 0));
		mapLocation.setLongitude(data.getDoubleExtra(LON_EXTRA, 0));
		setLocation(mapLocation);
		return true;
	}
	
	public void updateLocation() {
		if(locMgr == null) {
			return;
		}
		Location location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location != null) {
			setLocationFromGPS(location);
		}
	}

	public void resume() {
		locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10.0E3f, onLocationChange);
	}
	
	public void pause() {
		locMgr.removeUpdates(onLocationChange);
	}
	
	// =====================
	// GuiField Interface
	// =====================
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return labelValue;
	}

	@Override
	public String getValue() {
		return editText.getText().toString();
	}

	@Override
	public View getView() {
		return viewGroup;
	}

	@Override
	public void setValue(String value) {
		editText.setText(value);
		location = Util.toLocation(value);
	}
	
	@Override
	public void makeReadOnly() {
		//replace with a TextView
		String value = getValue();
		viewGroup.removeView(viewGroup.findViewById(R.id.location_view_values));
		editText = new TextView(viewGroup.getContext());
		viewGroup.addView(editText);
		setValue(value);
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public String getTag() {
		return AmmoParser.LOCATION_TAG;
	}

}
