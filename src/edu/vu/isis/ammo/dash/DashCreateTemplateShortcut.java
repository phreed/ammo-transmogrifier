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

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import edu.vu.isis.ammo.dash.DashCreateShortcut.ShortcutRow;
import edu.vu.isis.ammo.dash.template.AmmoTemplateManagerActivity;

public class DashCreateTemplateShortcut extends ListActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter();

		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent data = ((ShortcutRow)parent.getItemAtPosition(position)).getData();
				
				//user selected, setResult and finish
				setResult(RESULT_OK, data);
				finish();
			}
		});

	}
	
	private void setListAdapter() {
		List<ShortcutRow> list = new ArrayList<ShortcutRow>();
		
		for(String file : AmmoTemplateManagerActivity.getTemplateFiles(this)) {
			String name = file;
			name = name.replaceFirst("\\.xml$", "");  //case sensitive
			ShortcutRow row = new ShortcutRow(this, R.drawable.template_button2, name + " template", name, setupIntent(file));
			list.add(row);
		}

		setListAdapter(new SimpleAdapter(this, list,
				R.layout.pick_item, DashCreateShortcut.DASH_FIELD_NAMES, DashCreateShortcut.DASH_FIELD_RESOURCES));
	}

	private Intent setupIntent(String template) {
		return new Intent(this, AmmoTemplateManagerActivity.class)
				.putExtra(AmmoTemplateManagerActivity.TEMPLATE_EXTRA, template)
				.putExtra(AmmoTemplateManagerActivity.OPEN_FOR_EDIT_EXTRA, true);
	}
}
