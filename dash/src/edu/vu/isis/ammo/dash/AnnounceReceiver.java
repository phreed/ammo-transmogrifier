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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import edu.vu.isis.ammo.INetPrefKeys;
import edu.vu.isis.ammo.IntentNames;
import edu.vu.isis.ammo.api.AmmoPreference;
import edu.vu.isis.ammo.api.AmmoRequest;
import edu.vu.isis.ammo.api.IAmmoRequest;
import edu.vu.isis.ammo.api.type.Limit;
import edu.vu.isis.ammo.api.type.Query;
import edu.vu.isis.ammo.dash.preferences.DashPreferences;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.EventTableSchema;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.MediaTableSchema;
import edu.vu.isis.ammo.dash.provider.IncidentSchemaBase;

/**
 * Does this obviate the need for
 * SubscriptionViewer.java::makeDashSubscriptions()? At issue is the first
 * subscription.
 * 
 * This broadcast receiver catches certain intents broadcast by AmmoCore
 * announcing when connectivity has changed. Once the intents are caught,
 * certain tasks are performed like updating subscriptions and pull new content
 * from the Gateway.
 * 
 */
public class AnnounceReceiver extends BroadcastReceiver {
	private static final Logger logger = LoggerFactory.getLogger("class.AnnounceReceiver");

	static final String CORE_OPERATOR_ID = "CORE_OPERATOR_ID";
	static final String CORE_SUBSCRIPTION_DONE = "CORE_SUBSCRIPTION_DONE";

	private AmmoRequest.Builder ad;

	/**
	 * when a login intent is detected change the subscription.
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		this.ad = AmmoRequest.newBuilder(context, this);
		final String action = intent.getAction();
		WorkflowLogger.log("Announce Receiver - got an intent with action: " + action);
		if (IntentNames.AMMO_READY.endsWith(action)) {
			logger.info("Announce Receiver: Got an Intent");
			this.makeDashSubscriptions(context);
		}
		if (IntentNames.AMMO_CONNECTED.endsWith(action)) {
			logger.info("Announce Receiver: Got an Intent AMMO_CONNECTED");
			this.pullRecentReports(context);
		}
		// this.ad.releaseInstance();
	}

	public void makeDashSubscriptions(Context context) {
		final String userId = AmmoPreference
                   .getInstance(context)
                   .getString(INetPrefKeys.CORE_OPERATOR_ID, 
                              INetPrefKeys.DEFAULT_CORE_OPERATOR_ID);
		WorkflowLogger.log("Announce Receiver - making Dash subscriptions");

		try {
			this.ad.provider(EventTableSchema.CONTENT_URI).topic(EventTableSchema.CONTENT_TOPIC).subscribe();
			this.ad.provider(MediaTableSchema.CONTENT_URI).topic(MediaTableSchema.CONTENT_TOPIC).subscribe();
			this.ad.provider(EventTableSchema.CONTENT_URI).topic(EventTableSchema.CONTENT_TOPIC + "/" + IDash.MIME_TYPE_EXTENSION_TIGR_UID + "/" + userId).subscribe();
			this.ad.provider(MediaTableSchema.CONTENT_URI).topic(MediaTableSchema.CONTENT_TOPIC + "/" + IDash.MIME_TYPE_EXTENSION_TIGR_UID + "/" + userId).subscribe();
		} catch (RemoteException ex) {
			logger.error("could not connect to ammo {}", ex.getStackTrace());
		}
	}

	public void pullRecentReports(Context context) {
		WorkflowLogger.log("Announce Receiver - pulling recent reports");
		this.pullIncidentContent(context, EventTableSchema.CONTENT_URI, EventTableSchema.CONTENT_TOPIC, EventTableSchema._ID, EventTableSchema._RECEIVED_DATE);
		this.pullIncidentContent(context, MediaTableSchema.CONTENT_URI, MediaTableSchema.CONTENT_TOPIC, MediaTableSchema._ID, MediaTableSchema._RECEIVED_DATE);
	}

	/**
	 * Query expected in seconds. Query is in the format of “<URI>,<Origin
	 * user>,<Timestamp lower bound>,<Timestamp upper bound>,<Destination user>”
	 * When the 'Timestamp lower bound' is negative it will be interpreted as
	 * relative to the current time.
	 * 
	 * TODO Why is the query made at all if the time is not less than 0?
	 * 
	 * @param context
	 * @param contentUri
	 * @param contentTopic
	 * @param idField
	 * @param receivedDateField
	 */
	private void pullIncidentContent(Context context, Uri contentUri, String contentTopic, String idField, String receivedDateField) {
		final ContentResolver cr = context.getContentResolver();
		final String[] projection = { idField, receivedDateField };
		final String selection = new StringBuilder().append('"').append(IncidentSchemaBase.EventTableSchemaBase._DISPOSITION).append('"').append("=").append('\'').append(IncidentSchemaBase.Disposition.REMOTE).append('\'').toString();
		final String order = receivedDateField + " DESC";
		final Cursor cur = cr.query(contentUri, projection, selection, null, order);

		// Negative value indicates relative time.
		final long relativeTime;
		// If the query failed or the there are no items in the database, set
		// the relative time to -2000.
		if (cur == null || cur.getCount() == 0) {
			relativeTime = 0;
		} else {
			// Otherwise, subtract the received date
			cur.moveToFirst();
			long currentTime = System.currentTimeMillis();
			long timestamp = cur.getLong(cur.getColumnIndex(receivedDateField));
			if (timestamp < 1) {
				relativeTime = 1;
			} else {
				relativeTime = timestamp - currentTime;
			}
		}

		final String query;
		if (relativeTime >= 0) {
			query = ",,-1,,";
		} else {
			query = ",," + String.valueOf((relativeTime / 1000) - 1) + ",,";
		}

		// Get our dash limit count from shared preferences.
		final String limitStr = PreferenceManager
				.getDefaultSharedPreferences(context)
				.getString(DashPreferences.PREF_DASH_LIMIT, 
						DashPreferences.DEFAULT_PREF_DASH_LIMIT);
		final int dashLimitCount = Integer.valueOf(limitStr).intValue();

		try {
			final IAmmoRequest pull = ad.provider(contentUri).topic(contentTopic).limit(new Limit(dashLimitCount))
			// .expire( new new TimeStamp(Calendar.HOUR, 1, 0.0)
					.select(new Query(query)).retrieve();
			WorkflowLogger.log("AnnounceReceiver - pulling incident content with pull request: " + pull.toString() + " query: " + query);
			logger.trace("the query {} {}", pull, query);
		} catch (RemoteException ex) {
			logger.warn("ammo not available");
			if (cur != null)
				cur.close();
			return;
		}

		WorkflowLogger.log("AnnounceReceiver - pull succeeded with uri: " + contentUri);
		logger.debug("pull succeeded with uri: " + contentUri.toString());
		if (cur != null) {
			cur.close();
		}

	}

}
