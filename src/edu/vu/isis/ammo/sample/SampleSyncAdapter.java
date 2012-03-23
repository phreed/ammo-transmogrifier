package edu.vu.isis.ammo.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import edu.vu.isis.ammo.api.IDistributorAdaptor;

public class SampleSyncAdapter extends Service {
	
	/**
	 * In order for the service to be shutdown cleanly the 'serviceStart()'
	 * method may be used to prepare_for_stop, it will be stopped shortly and it
	 * needs to have some things done before that happens.
	 * 
	 * When the user changes the configuration 'startService()' is run to change
	 * the settings.
	 */
	
	private static final Logger logger = LoggerFactory.getLogger("ammo-sample");
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			final String action = intent.getAction();
			if (action != null) {
				
			}
		}
		return Service.START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	
		return new IDistributorAdaptor.Stub() {

			@Override
			public byte[] serialize(String encoding, String relationName,
					String tupleId) throws RemoteException {
				// TODO make call through content resolver
				logger.info("Serialize Called {} {}", encoding, relationName);
				return null;
			}

			@Override
			public void deserialize(String encoding, String relationName,
					byte[] payload) throws RemoteException {
				// TODO make call through content resolver
				logger.info("DeSerialize Called, {} {}", encoding, relationName);
				
				// FIXME write to the custom provider address
				/*
				final Uri customProvider = encoding.extendProvider(provider);
				final ContentValues cv = new ContentValues();
				cv.put("data", data);
				return resolver.insert(customProvider, cv);
				*/

			}
			
		};
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	}

}
