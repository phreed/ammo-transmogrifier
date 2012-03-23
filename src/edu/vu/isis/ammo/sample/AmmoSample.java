package edu.vu.isis.ammo.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import edu.vu.isis.ammo.api.AmmoRequest;
import edu.vu.isis.ammo.api.IAmmoRequest;
import edu.vu.isis.ammo.sample.provider.SampleSchemaBase;

public class AmmoSample extends Activity {
	private static final Logger logger = LoggerFactory.getLogger(AmmoSample.class);
	
	// Members.
	private EditText latitudeField;
	private EditText logitudeField;
	private Button saveActor;
	private AmmoRequest.Builder builder;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        builder = AmmoRequest.newBuilder(this);
        
        latitudeField = (EditText)findViewById(R.id.latitudeEditText);
        logitudeField = (EditText)findViewById(R.id.longitudeEditText);
        saveActor = (Button)findViewById(R.id.saveButton);
        
        saveActor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				postContent();
			}
        });
        
    }
    
    /**
     * Read the contents of the latitude and longitude text fields. Use the Ammo API to post 
     * their contents to the generated content provider.
     */
    public void postContent() {
    	final double latitude = Double.valueOf(latitudeField.getText().toString()).doubleValue();
    	final double longitude = Double.valueOf(logitudeField.getText().toString()).doubleValue();

    	
    	final ContentValues cv = new ContentValues();
    	cv.put(SampleSchemaBase.LocationTableSchemaBase.LAT, latitude);
    	cv.put(SampleSchemaBase.LocationTableSchemaBase.LON, longitude);
    	
    	final ContentResolver cr = this.getContentResolver();
    	final Uri uri = cr.insert(SampleSchemaBase.LocationTableSchemaBase.CONTENT_URI, cv);
    	try {
			final IAmmoRequest request = builder.provider(uri).topic(SampleSchemaBase.LocationTableSchemaBase.CONTENT_TOPIC).post();
			// Use request handle to get different information about the request...
			logger.info("request {} ", request );
			
		} catch (RemoteException ex) {
			logger.error("could not update provider {}", ex.getStackTrace());
		}
    }
}