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

import java.util.Arrays;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Creates a shortcut to
 * For more information on android shortcuts, read:
 * http://developer.android.com/reference/android/content/Intent.html#ACTION_CREATE_SHORTCUT
 */
public class DashCreateShortcut extends ListActivity {

	public static final String[] DASH_FIELD_NAMES = new String[]{"icon", "text1"};
	public static final int[] DASH_FIELD_RESOURCES = new int[]{android.R.id.icon, android.R.id.text1};
	private ShortcutRow templateRow;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter();

		getListView().setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent data = ((ShortcutRow)parent.getItemAtPosition(position)).getData();
				
				if(((ShortcutRow)parent.getItemAtPosition(position)) == templateRow) {
					Intent intent = (Intent)data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
					if(intent != null) {
						startActivityForResult(intent, 0);
					}
					return;
				}
				
				//user selected, setResult and finish
				setResult(RESULT_OK, data);
				finish();
			}
		});

	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != RESULT_CANCELED) {
			setResult(RESULT_OK, data);
			finish();
		}
	}

	private void setListAdapter() {
		ShortcutRow cameraRow = new ShortcutRow(this, R.drawable.camera_button2, "Take pictures", "Camera", setupIntent(Dash.IMAGE_TYPE));
		ShortcutRow audioRow = new ShortcutRow(this, R.drawable.audio_button2, "Record audio", "Audio", setupIntent(Dash.AUDIO_TYPE));
		templateRow = new ShortcutRow(this, R.drawable.template_button2, "Fill in templates", "Template", setupCreateTemplateShortcut());

		setListAdapter(new SimpleAdapter(this, Arrays.asList(cameraRow, audioRow, templateRow),
				R.layout.pick_item, DASH_FIELD_NAMES, DASH_FIELD_RESOURCES));
	}

	private Intent setupIntent(int dashMode) {
		return new Intent(this, Dash.class).putExtra(Dash.MODE, dashMode);
	}
	
	private Intent setupCreateTemplateShortcut() {
		return new Intent(this, DashCreateTemplateShortcut.class);
	}
	
	static class ShortcutRow extends HashMap<String, Object> {
		private static final long serialVersionUID = 1L;
		private Intent data = new Intent();
		/**
		 * @param icon the icon to display in the list and the final icon
		 * @param listText the text to put in the list
		 * @param shortcutText the text to put on the icon
		 * @param shortcutIntent the intent to start
		 */
		public ShortcutRow(Context context, int icon, String listText, String shortcutText, Intent shortcutIntent) {
			put("icon", icon);
			put("text1", listText);
			data.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
			data.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutText);
			data.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, ShortcutIconResource.fromContext(context, icon));
		}
		
		public Intent getData() {
			return data;
		}
	}
}
