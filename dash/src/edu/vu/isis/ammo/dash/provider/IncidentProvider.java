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
package edu.vu.isis.ammo.dash.provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.CategoryTableSchema;
import edu.vu.isis.ammo.dash.provider.IncidentSchemaBase.CategoryTableSchemaBase;

/**
 * Extends the basic incident provider with duplicate detection for reports.
 * 
 * In a previous lifetime, we also had categories associated with reports though
 * that functionality has since been removed.
 * 
 */
public class IncidentProvider extends IncidentProviderBase {

	private static final Logger logger = LoggerFactory.getLogger("class.IncidentProvider");

	public static final String MAIN_ICON = "MainIcon";
	public static final int RESULT_CATEGORY_LOAD_SUCCESS = 1;
	public static final int RESULT_CATEGORY_LOAD_FAIL = -1;
	public static final String EXTRA_LOAD_RESULT = "load_result";

	protected class IncidentDatabaseHelper extends IncidentProviderBase.IncidentDatabaseHelper {
		protected IncidentDatabaseHelper(Context context) {
			// super(context, IncidentSchema.DATABASE_VERSION);
			
			// Cause a memory based content provider to be used
			super(context, (String) null, (SQLiteDatabase.CursorFactory) null, IncidentSchema.DATABASE_VERSION);
		}
	}

	// ===========================================================
	// Content Provider Overrides
	// ===========================================================

	@Override
	public boolean onCreate() {
		super.onCreate();
		
		/**
		 * Introduce a broadcast receiver to re-populate the category table.
		 */
		final BroadcastReceiver controller = new BroadcastReceiver() {
			@Override
			public void onReceive(final Context context, Intent intent) {
				final IncidentProvider provider = IncidentProvider.this;
				final SQLiteDatabase db = provider.openHelper.getWritableDatabase();
				final PendingIntent pendingIntent = (PendingIntent) intent.getParcelableExtra("pending_intent");
				// We should load the categories in a background thread.
				new Thread(new Runnable() {
					@Override
					public void run() {
						final Intent dummyIntent = new Intent("edu.vu.isis.ammo.dash.Dash.NONE");
						try {
							pendingIntent.send(context, RESULT_CATEGORY_LOAD_SUCCESS, dummyIntent);
						} catch (CanceledException e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		};
		
		final IntentFilter filter = new IntentFilter();
		filter.addAction(IncidentSchema.CategoryTableSchema.RELOAD);
		
	    getContext().registerReceiver(controller, filter);
	    
	    
		return true;
	}

	@Override
	public boolean createDatabaseHelper() {
		this.openHelper = new IncidentProvider.IncidentDatabaseHelper(getContext());
		return true;
	}

	// ===========================================================
	// Helper methods
	// ===========================================================

}
