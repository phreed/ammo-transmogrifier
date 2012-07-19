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

import java.util.List;

import org.w3c.dom.Element;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.template.parsing.AmmoParser;
import edu.vu.isis.ammo.dash.template.parsing.ViewParsers;

/**
 * Collapseable view used to display a predefined set of values.
 * @author demetri
 *
 */
public class PulldownView implements GuiField {

	// =====================
	// Fields
	// =====================
	private Spinner spinner;
	private TextView textView;
	private ViewGroup viewGroup;
	private List<String> values;
	private String id, labelValue;
	
	// =====================
	// Lifecycle
	// =====================
	public PulldownView(Context context, Element eNode) {
		values = ViewParsers.getTextFromChildNodes(eNode);
		
		viewGroup = (ViewGroup)LayoutInflater.from(context).inflate(R.layout.template_pulldown_view, null);
		TextView label = (TextView)viewGroup.findViewById(R.id.pulldown_view_label);
		
		id = eNode.getAttribute(AmmoParser.ID_FIELD);
		labelValue = eNode.getAttribute(AmmoParser.LABEL_FIELD);
		if(labelValue == null || labelValue.length() == 0) {
			labelValue = id;
		}
		label.setText(labelValue + ":");
		
		if (values.isEmpty()) return;
		spinner = (Spinner)viewGroup.findViewById(R.id.pulldown_view_spinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, values);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}
	
	@Override
	public View getView() {
		return viewGroup;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getValue() {
		if(spinner != null) {
			return (String)spinner.getSelectedItem();
		}
		if(textView != null) {
			return textView.getText().toString();
		}
		return null;
	}
	
	@Override
	public void setValue(String value) {
		if(spinner != null) {
			int index = values.indexOf(value);
			spinner.setSelection(index >= 0 ? index : 0);
		}
		if(textView != null) {
			textView.setText(value);
		}
	}

	@Override
	public String getLabel() {
		return labelValue;
	}
	
	@Override
	public void makeReadOnly() {
		//replace with a TextView
		String value = getValue();
		viewGroup.removeView(spinner);
		textView = new TextView(viewGroup.getContext());
		spinner = null;
		viewGroup.addView(textView);
		setValue(value);
	}

	@Override
	public String getTag() {
		return AmmoParser.PULLDOWN_TAG;
	}
}
