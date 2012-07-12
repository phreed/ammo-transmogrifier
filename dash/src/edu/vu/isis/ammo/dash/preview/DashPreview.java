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
package edu.vu.isis.ammo.dash.preview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import edu.vu.isis.ammo.dash.Dash;
import edu.vu.isis.ammo.dash.IDash;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.Util;
import edu.vu.isis.ammo.dash.WorkflowLogger;
import edu.vu.isis.ammo.dash.dialogs.PreviewDialog;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.EventTableSchema;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.MediaTableSchema;
import edu.vu.isis.ammo.dash.template.AmmoTemplateManagerActivity;

/**
 * List activity that displays all reports currently in the backing content
 * provider. From this preview activity, media associated with reports may be
 * previewed by long-pressing the list item on screen. Type resolution of media
 * items occurs on the fly.
 * 
 * @author demetri
 * 
 */
public class DashPreview extends ListActivity {
	private static final Logger logger = LoggerFactory.getLogger("class.Preview");
	private static final int MENU_CONTEXT_PREVIEW_MEDIA = Menu.FIRST;
	private static final int MENU_CONTEXT_PREVIEW_TEMPLATE = MENU_CONTEXT_PREVIEW_MEDIA + 1;
	private static final int DIALOG_PREVIEW_ENTRY = 0;
	private static final int MENU_OPTION_PURGE = 1;

	private Cursor mCursor;
	private DashPreviewCursorAdapter cursorAdapter;
	private int previewIntDataType;
	private String previewDataPath;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dash_preview);
		WorkflowLogger.log("DashPreview - onCreate");
		this.setupListView();
	}

	@Override
	public void onStart() {
		super.onStart();
		cursorAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCursor.close();
	}

	// ===========================================================
	// Options menu
	// ===========================================================
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_OPTION_PURGE, Menu.NONE, "Remove All Dashes");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_OPTION_PURGE:
			// Show an alert dialog verifying this action is indeed what we want
			// to do.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Are you sure?").setMessage("This will permanently remove all dashes, dash templates, and their associated media.").setCancelable(true).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					purgeAllDashes();
				}
			}).setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
			break;
		default:
			// do nothing.
		}

		return true;
	}

	// As the name suggests, remove all dash and dash templates and their
	// associated media from
	// the database.
	protected void purgeAllDashes() {
		ContentResolver cr = this.getContentResolver();
		// Remove events first.
		int numEventsDeleted = cr.delete(EventTableSchema.CONTENT_URI, null, null);

		// Remove media referenced in the table.
		String[] mediaProjection = { MediaTableSchema.DATA };
		Cursor mediaCursor = cr.query(MediaTableSchema.CONTENT_URI, mediaProjection, null, null, null);
		while (mediaCursor.moveToNext()) {
			String filePath = mediaCursor.getString(mediaCursor.getColumnIndex(MediaTableSchema.DATA));
			File file = new File(filePath);
			boolean didDeleteSuccessfully = file.delete();
			if (!didDeleteSuccessfully) {
				logger.error("Error purging media from SD card");
			}
		}

		// Remove the media table contents itself.
		int numMediaRowsDeleted = cr.delete(MediaTableSchema.CONTENT_URI, null, null);
		
		// Notify the user the delete has occurred and refresh the table.
		Toast.makeText(this, "Removed " + numEventsDeleted + " events and " + numMediaRowsDeleted + " media items.", Toast.LENGTH_LONG).show();
		WorkflowLogger.log("DashPreview - Removed " + numEventsDeleted + " events and " + numMediaRowsDeleted + " media items.");
		cursorAdapter.notifyDataSetChanged();
	}

	// ===========================================================
	// Dialog/Menu Management
	// ===========================================================
	@Override
	protected Dialog onCreateDialog(int id, Bundle bundle) {
		PreviewDialog pd = new PreviewDialog(this);
		pd.setOwnerActivity(this);

		return pd;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle bundle) {
		super.onPrepareDialog(id, dialog, bundle);
		if (!(dialog instanceof PreviewDialog) || previewDataPath == null) {
			logger.error("Could not prepare dialog.  Bad state.");
			Util.makeToast(this, "Could not find media to display");
			return;
		}

		PreviewDialog pd = (PreviewDialog) dialog;
		pd.setDataAndDataType(previewDataPath, previewIntDataType);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		final int lastSelectedRowPos = ((AdapterContextMenuInfo) menuInfo).position;

		// Check what kind of media we have (if any). Either present a menu to
		// select media type
		// or if only one type, immediately show it.
		final boolean hasTemplate = hasTemplate(lastSelectedRowPos);
		final boolean hasMedia = hasMedia(lastSelectedRowPos);

		// only display a menu if we must
		if (!hasMedia && !hasTemplate) {
			Toast.makeText(this, "No media available", Toast.LENGTH_SHORT).show();
			return;
		}

		if (hasMedia && hasTemplate) {
			menu.add(ContextMenu.NONE, MENU_CONTEXT_PREVIEW_MEDIA, MENU_CONTEXT_PREVIEW_MEDIA, R.string.menu_preview_media);
			menu.add(ContextMenu.NONE, MENU_CONTEXT_PREVIEW_TEMPLATE, MENU_CONTEXT_PREVIEW_TEMPLATE, R.string.menu_preview_template);
		} else if (hasMedia) {
			preview(lastSelectedRowPos, false);
		} else if (hasTemplate) {
			preview(lastSelectedRowPos, true);
		}
	}

	private boolean hasMedia(int lastSelectedRowPos) {
		return cursorAdapter.getDataForEventId(lastSelectedRowPos, false) != null;
	}

	private boolean hasTemplate(int lastSelectedRowPos) {
		return cursorAdapter.getDataForEventId(lastSelectedRowPos, true) != null;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int lastSelectedRowPos = ((AdapterContextMenuInfo) item.getMenuInfo()).position;

		switch (item.getItemId()) {

		case MENU_CONTEXT_PREVIEW_MEDIA: // Launch activity to show a preview of
											// the media selected.
			return preview(lastSelectedRowPos, false);

		case MENU_CONTEXT_PREVIEW_TEMPLATE: // Launch activity to show a preview
											// of the template selected.
			return preview(lastSelectedRowPos, true);

		default:
			return super.onContextItemSelected(item);
		}
	}

	private boolean preview(int position, boolean template) {
		String dataType = cursorAdapter.getDataTypeForEventId(position, template);
		previewDataPath = cursorAdapter.getDataForEventId(position, template);
		if (dataType == null || previewDataPath == null || !new File(previewDataPath).exists()) {
			String name = template ? "template" : "media";
			Toast.makeText(this, name + " not" + " available", Toast.LENGTH_SHORT).show();
			return true;
		}
		if (dataType.equals(MediaTableSchema.TEXT_DATA_TYPE)) {
			previewIntDataType = Dash.TEXT_TYPE;
		} else if (dataType.equals(MediaTableSchema.AUDIO_DATA_TYPE)) {
			previewIntDataType = Dash.AUDIO_TYPE;
		} else if (dataType.equals(MediaTableSchema.IMAGE_DATA_TYPE)) {
			previewIntDataType = Dash.IMAGE_TYPE;
		} else if (dataType.equals(MediaTableSchema.VIDEO_DATA_TYPE)) {
			// Since video isn't using the alert dialog for previews, we'll
			// launch the intent here along with the video uri.
			Intent videoPreviewIntent = new Intent(IDash.VIDEO_PREVIEW_INTENT);
			videoPreviewIntent.putExtra(Dash.FILE_URI, previewDataPath);
			this.startActivity(videoPreviewIntent);
			return true;
		} else if (dataType.equals(MediaTableSchema.TEMPLATE_DATA_TYPE)) {
			// Get the string from data path.
			File file = new File(previewDataPath);
			String jsonData = null;
			byte[] bytes = new byte[(int) file.length()];
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				int bytesRead = 1;
				while (bytesRead > 0) {
					bytesRead = fis.read(bytes);
				}

				jsonData = new String(bytes);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				//try {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException ex) {
                        logger.error("could not close {}", file, ex);
					}
			}

			WorkflowLogger.log("DashPreview - previewing data with type: " + dataType + " at path: " + previewDataPath);
			Intent i = new Intent(this, AmmoTemplateManagerActivity.class);
			i.putExtra(AmmoTemplateManagerActivity.JSON_DATA_EXTRA, jsonData);
			i.putExtra(AmmoTemplateManagerActivity.OPEN_FOR_EDIT_EXTRA, false);
			this.startActivity(i);
			return true;
		}

		showDialog(DIALOG_PREVIEW_ENTRY);
		return true;
	}

	private void setupListView() {
		String selection = EventTableSchema.STATUS + " != " + EventTableSchema.STATUS_DRAFT;
		String sortOrder = EventTableSchema.CREATED_DATE + " DESC";
		mCursor = this.managedQuery(EventTableSchema.CONTENT_URI, null, selection, null, sortOrder);

		if (mCursor.getCount() == 0) {
			logger.debug("::setupListView - no items in postalSchema cursor on init query");
			findViewById(R.id.no_media_found_text).setVisibility(View.VISIBLE);
		}

		// Now create a new list adapter bound to the cursor.
		// SimpleListAdapter is designed for binding to a Cursor.
		cursorAdapter = new DashPreviewCursorAdapter(this, // Context.
				R.layout.dash_preview_cell, // Specify the row template to use
				mCursor, // Pass in the cursor to bind to.
				// Array of cursor columns to bind to.
				this.getViewBindingFrom(),
				// Parallel array of which template objects to bind to those
				// columns.
				this.getViewBindingTo(), false, true, true);

		// Bind to our new adapter.
		setListAdapter(cursorAdapter);
		this.registerForContextMenu(this.getListView());
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				openContextMenu(view);
			}
		});
	}

	private String[] getViewBindingFrom() {
		return new String[] {};
	}

	private int[] getViewBindingTo() {
		return new int[] {};
	}
}
