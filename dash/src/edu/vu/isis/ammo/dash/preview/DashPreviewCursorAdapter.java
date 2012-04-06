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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import edu.vu.isis.ammo.dash.LocationTextView;
import edu.vu.isis.ammo.dash.R;
import edu.vu.isis.ammo.dash.Util;
import edu.vu.isis.ammo.dash.provider.IncidentSchema;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.EventTableSchema;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.MediaTableSchema;

/**
 * Not multithreaded. For non-concurrent use.
 * 
 * This adapter is used by DashPreview to format and populate the ListView items
 * displayed.
 */
public class DashPreviewCursorAdapter extends SimpleCursorAdapter {

	private static final Logger logger = LoggerFactory.getLogger("class.PreviewCursorAdapter");
	// private Cursor mCursor;
	private Context mContext;

	private boolean isMGRSCoordinateType;
	private boolean isEventCursor;

	/**
	 * Create the adapter and set certain flags.
	 * 
	 * @param context
	 * @param layout
	 * @param c
	 * @param from
	 * @param to
	 * @param isSubmissionMode
	 * @param isMGRSCoordinateType
	 * @param isEventCursor
	 *            - If the cursor passed in points to event cursor, set to true.
	 *            Otherwise, we're expecting a postal cursor and the parameter
	 *            should be false.
	 */
	public DashPreviewCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, boolean isSubmissionMode, boolean isMGRSCoordinateType, boolean isEventCursor) {
		super(context, layout, c, from, to);
		mContext = context;
		this.isMGRSCoordinateType = isMGRSCoordinateType;
		this.isEventCursor = isEventCursor;
	}

	// Move the cursor to the position passed and return that row's data type.
	// If position is -1, move the cursor to the current focused position.
	public String getDataTypeForEventId(int pos, boolean template) {
		return getMediaString(pos, IncidentSchema.MediaTableSchema.DATA_TYPE, getTemplateSelection(template));
	}

	public String getDataForEventId(int pos, boolean template) {
		return getMediaString(pos, IncidentSchema.MediaTableSchema.DATA, getTemplateSelection(template));
	}

	private String getMediaString(int pos, String columnName, String selection) {
		Cursor eventCursor = getEventCursor(mContext, super.getCursor());
		eventCursor.moveToPosition(pos);

		// Determine what type of data is at that position.
		String uuid = eventCursor.getString(eventCursor.getColumnIndex(EventTableSchema.UUID));

		if (selection == null) {
			selection = "TRUE";
		}
		selection += " AND " + MediaTableSchema.EVENT_ID + "=?";

		Cursor c = mContext.getContentResolver().query(MediaTableSchema.CONTENT_URI, null, selection, new String[] { uuid }, null);
		// If cursor isn't valid, quit right here.
		if (c == null) {
			return null;
		} else if (c.getCount() == 0) {
			c.close();
			return null;
		}
		c.moveToFirst();
		String value = c.getString(c.getColumnIndex(columnName));

		// If this is an empty string, return null.
		if (value == null || value.length() == 0) {
			value = null;
		}
		c.close();
		return value;
	}

	private static String getTemplateSelection(boolean template) {
		String operation = template ? "=" : "!=";
		return MediaTableSchema.DATA_TYPE + operation + "'" + MediaTableSchema.TEMPLATE_DATA_TYPE + "'";
	}

	// ===========================================================
	// UI Management
	// ===========================================================
	/**
	 * Gets the eventCursor from whatever URI is stored in the row the
	 * distributorCursor currently points to. Returns the cursor already moved
	 * to the first position.
	 */
	private Cursor getEventCursor(Context context, Cursor distributorCursor) {
		Cursor eventCursor = null;
		if (isEventCursor) {
			eventCursor = super.getCursor();
		}

		return eventCursor;
	}

	// Override this method so we can set the category image and date
	// for each report.
	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		super.bindView(v, context, cursor);
		Cursor eventCursor = getEventCursor(context, cursor);
		if (eventCursor == null) {
			logger.error("Event cursor null. Not loading view. This could be due to out of sync entries in the postal table.");
			return;
		} else if (eventCursor.getCount() == 0) {
			logger.error("Event cursor empty. Not loading view. This could be due to out of sync entries in the postal table.");
			return;
		}

		this.bindCommentToView(v, context, eventCursor);
		this.bindAuthorToView(v, context, eventCursor);
		this.bindDateToView(v, context, eventCursor);
		this.bindReportIconToView(v, context, eventCursor);
		this.bindLocationToView(v, context, eventCursor);
	}

	/**
	 * Get the comment out of the event table and add it to the view.
	 * 
	 * @param v
	 * @param context
	 * @param eventCursor
	 */
	public void bindCommentToView(View v, Context context, Cursor eventCursor) {
		String comment = eventCursor.getString(eventCursor.getColumnIndex(EventTableSchema.DESCRIPTION));
		TextView tvDesc = (TextView) (v.findViewById(R.id.report_browser_cell_report_comment));
		tvDesc.setText(comment);
	}

	/**
	 * Get the comment out of the event table and add it to the view.
	 * 
	 * @param v
	 * @param context
	 * @param eventCursor
	 */
	public void bindAuthorToView(View v, Context context, Cursor eventCursor) {
		String author = eventCursor.getString(eventCursor.getColumnIndex(EventTableSchema.ORIGINATOR));
		TextView tvAuthor = (TextView) (v.findViewById(R.id.report_browser_cell_report_author));
		tvAuthor.setText(author);
	}

	/**
	 * Set the image based on the status of the report (sent or pending)
	 * 
	 * @param v
	 * @param context
	 * @param cursor
	 */
	public void bindReportIconToView(View v, Context context, Cursor eventCursor) {
		// Get a cursor to the local event table from the distributor cursor.
		ContentResolver cr = context.getContentResolver();
		int mediaCount = eventCursor.getInt(eventCursor.getColumnIndex(EventTableSchema.MEDIA_COUNT));
		ImageView iv = (ImageView) v.findViewById(R.id.report_browser_cell_media_icon);
		if (mediaCount == 0) {
			// Set the ImageView to empty each time. I think the ListView does
			// some optimizations in
			// the background so this is the best way to counter that.
			iv.setImageResource(R.drawable.empty_media);
			return;
		} else {
			iv.setImageResource(R.drawable.button_missing);
		}

		String incidentUUID = eventCursor.getString(eventCursor.getColumnIndex(EventTableSchema.UUID));
		String mediaSelection = MediaTableSchema.EVENT_ID + "='" + incidentUUID + "'";
		Cursor mediaCursor = cr.query(MediaTableSchema.CONTENT_URI, null, mediaSelection, null, null);

		// If the media for this event hasn't been received (i.e. We have a
		// media count, but there's not media in the table)
		// show an icon indicating as such.
		int resId = -1;
		if (mediaCursor == null) {
			// do nothing for now.
		} else if (mediaCursor.getCount() == 0) {
			mediaCursor.close();
		} else {
			// Dashes only contain a single media item so we don't need to
			// iterate over the cursor.
			mediaCursor.moveToFirst();
			String dataType = mediaCursor.getString(mediaCursor.getColumnIndex(MediaTableSchema.DATA_TYPE));
			String filePath = mediaCursor.getString(mediaCursor.getColumnIndex(MediaTableSchema.DATA));
			boolean mediaExists = new File(filePath).exists();

			// Set the icon. If the media file isn't on the SD card, display a
			// different drawable.
			if (dataType.equals(MediaTableSchema.AUDIO_DATA_TYPE)) {
				resId = mediaExists ? R.drawable.button_audio : R.drawable.button_audio_missing;
			} else if (dataType.equals(MediaTableSchema.IMAGE_DATA_TYPE)) {
				resId = mediaExists ? R.drawable.button_camera : R.drawable.button_camera_missing;
			} else if (dataType.equals(MediaTableSchema.VIDEO_DATA_TYPE)) {
				resId = mediaExists ? R.drawable.button_video : R.drawable.button_video_missing;
			} else if (dataType.equals(MediaTableSchema.TEMPLATE_DATA_TYPE)) {
				resId = mediaExists ? R.drawable.button_template : R.drawable.button_template_missing;
			}

			iv.setImageResource(resId);
			mediaCursor.close();
		}
	}

	public void bindDateToView(View v, Context context, Cursor eventCursor) {
		TextView tv = (TextView) v.findViewById(R.id.report_browser_date);

		// Get the created date from the cursor and format it for display in the
		// cell.
		long createdDate = eventCursor.getLong(eventCursor.getColumnIndex(EventTableSchema.CREATED_DATE));
		tv.setText(Util.formatTime(createdDate));
	}

	/**
	 * Set the MGRS location field based on the lat lon values saved in the row.
	 * 
	 * @param v
	 * @param context
	 * @param cursor
	 */
	public void bindLocationToView(View v, Context context, Cursor eventCursor) {
		LocationTextView ltvMGRS = (LocationTextView) v.findViewById(R.id.report_browser_cell_location_mgrs);
		LocationTextView ltvLat = (LocationTextView) v.findViewById(R.id.report_browser_location_latitude);
		LocationTextView ltvLon = (LocationTextView) v.findViewById(R.id.report_browser_location_longitude);
		LinearLayout ll = (LinearLayout) v.findViewById(R.id.report_browser_cell_location_container);

		if (isMGRSCoordinateType) {
			double lat = eventCursor.getDouble(eventCursor.getColumnIndex(EventTableSchema.LATITUDE));
			double lon = eventCursor.getDouble(eventCursor.getColumnIndex(EventTableSchema.LONGITUDE));
			ltvMGRS.setFormattedTextFromLocation(Util.buildLocation(lat, lon));

			if (ltvLat != null && ltvLon != null) {
				ll.removeView(ltvLat);
				ll.removeView(ltvLon);
			}
		} else {
			if (ltvMGRS != null)
				ll.removeView(ltvMGRS);
		}
	}
}
