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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * This is invoked with an intent with action ACTION_AUTHENTICATOR_INTENT. 
 * It instantiates the sync-adapter and returns its IBinder.
 */
public class IncidentSyncService extends Service {
	private final Logger logger = LoggerFactory.getLogger(IncidentSyncService.class);

	private static final Object locker = new Object();

	private static IncidentSyncAdaptor syncAdaptor = null;

	@Override
	public void onCreate() {
		synchronized (locker) {
			if (syncAdaptor == null) {
				syncAdaptor = new IncidentSyncAdaptor(getApplicationContext(), true);
			}
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		logger.info("on bind by {}", intent);
		return syncAdaptor.getSyncAdapterBinder();
	}
}
