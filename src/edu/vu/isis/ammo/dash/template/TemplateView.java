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
package edu.vu.isis.ammo.dash.template;

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.widget.TableLayout;
import android.widget.Toast;
import edu.vu.isis.ammo.dash.Dash;
import edu.vu.isis.ammo.dash.Util;
import edu.vu.isis.ammo.dash.WorkflowLogger;
import edu.vu.isis.ammo.dash.template.model.Record;
import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;
import edu.vu.isis.ammo.dash.template.view.GuiField;
import edu.vu.isis.ammo.dash.template.view.LocationView;

/**
 * Handles displaying the template once it has been parsed and also 
 * is responsible for initiating parsing either from scratch or from serialized data (read: json).
 * @author adrian
 * @author demetri
 *
 */
public class TemplateView extends TableLayout {

	private static final Logger logger = LoggerFactory.getLogger(TemplateView.class);

	private Record data;
	private String templateDisplayName;
	private Map<String, GuiField> guiFields;
	private boolean openForEdit;
	private Activity activity;
	public LocationView locationView;

	// =============================
	// Lifecycle
	// =============================
	/** Called when the activity is first created. */
	public TemplateView(Activity activity, boolean openForEdit) {
		super(activity);
		this.activity = activity;
		this.openForEdit = openForEdit;
		
		setColumnStretchable(1, true);
	}

	// =============================
	// UI Management
	// =============================
	public boolean loadTemplate(String templateFilename, Location location) {
		AmmoTemplateManagerActivity.checkFiles(activity);
		File templateFile = new File(Dash.TEMPLATE_DIR, templateFilename);
		WorkflowLogger.log("TemplateView - loading template with name: " + templateFile);
		StringBuilder templateDisplayNameHolder = new StringBuilder();
		guiFields = AmmoParser.parseXMLForFileIntoView(activity, templateFile, this, templateDisplayNameHolder, location);
		templateDisplayName = templateDisplayNameHolder.toString();
		
		if (guiFields == null) {
			Toast.makeText(activity, "Could not parse template file", Toast.LENGTH_SHORT).show();
			return false;
		}

		// We want to a handle to the location view in the template (if there is one). 
		// We're going to use the view when we save the template to set a location for the event.
		for (GuiField guiField : guiFields.values()) {
			if (guiField.getTag().equalsIgnoreCase(AmmoParser.LOCATION_TAG)) {
				locationView = (LocationView)guiField;
			}
		}
				
		
		// Now that we've built the layout, populate the contents.
		if (data == null) {
			data = new Record();
		} else {
			fromModel();
		}
		
		if (!openForEdit) {
			for (GuiField guiField : guiFields.values()) {
				guiField.makeReadOnly();
			}
		}
		
		WorkflowLogger.log("TemplateView - finished loading template files");
		
		return true;
	}
	


	public boolean loadTemplateFromJson(String jsonData, Location location) {
		//clear everything
		removeAllViews();
		
		Record data = AmmoTemplateManagerActivity.fromJson(jsonData);
		String template = data.getField(AmmoTemplateManagerActivity.TEMPLATE_NAME_KEY);
		if (template == null || template.length() == 0) {
			Util.makeToast(activity, "Could not load template. Filename not present.");
			return false;
		}
		if(!loadTemplate(template, location)) {
			return false;
		}
		
		setData(data);
		return true;
	}

	public String toText() {
		StringBuilder out = new StringBuilder();
		for (GuiField guiField : guiFields.values()) {
			out.append(guiField.getLabel());
			out.append(": ");
			out.append(guiField.getValue());
			out.append('\n');
		}
		return out.toString();
	}

	private void toModel() {
		if(guiFields == null || data == null) {
			return;
		}
		for (GuiField guiField : guiFields.values()) {
			data.setField(guiField.getId(), guiField.getValue());
		}
		data.setTemplateDisplayName(templateDisplayName);
	}

	private void fromModel() {
		if(guiFields == null || data == null) {
			return;
		}
		for (GuiField guiField : guiFields.values()) {
			guiField.setValue(data.getField(guiField.getId()));
		}
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != Activity.RESULT_OK) {
			//if the child activity was canceled, ignore.
			return;
		}
		if(requestCode != Dash.MAP_TYPE) {
			//wasn't for me
			return;
		}
		
		if(data == null || data.getStringExtra(AmmoTemplateManagerActivity.LOCATION_FIELD_ID_EXTRA) == null) {
			logger.error("The intent was lacking a location field id");
			Util.makeToast(getContext(), "Could not get map point");
			return;
		}
		
		String id = data.getStringExtra(AmmoTemplateManagerActivity.LOCATION_FIELD_ID_EXTRA);
		
		if(guiFields.get(id) == null || !(guiFields.get(id) instanceof LocationView)) {
			logger.error("The intent had a location field id that we could not find: " + id);
			Util.makeToast(getContext(), "Could not get map point");
			return;
		}
		
		
		if(!((LocationView)guiFields.get(id)).processMapPoint(data)) {
			//toast already displayed, and error log already has details on the error
		}
	}
	
	public Record getData() {
		toModel();
		return data;
	}
	
	public void setData(Record data) {
		this.data = data;
		fromModel();
	}
	
	public String getTemplateDisplayName() {
		return templateDisplayName;
	}
}