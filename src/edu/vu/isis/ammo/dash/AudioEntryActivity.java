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

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.vu.isis.ammo.dash.dialogs.IDialogHelper;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.MediaTableSchema;

/**
 * Activity that is launched when a user wants to add an audio clip to a report. This activity
 * looks like dialog box on screen. 
 * 
 * This activity is responsible for setting up the audio controls
 * and also handling persistence of audio data once recording is finished. The file path of the 
 * newly created audio file is returned upon activity finish. 
 * @author demetri
 *
 */
public class AudioEntryActivity extends Activity implements OnClickListener, OnInfoListener, IDialogHelper {
	// ===========================================================
	// Constants
	// ===========================================================
    private static final Logger logger = LoggerFactory.getLogger(AudioEntryActivity.class);
	private static final String DIRECTORY_AUDIO = "support/Dash/ammo_audio";
	public static String INCIDENT_UUID = "incident_uuid";
	
	/** View tags */
	private static final int START_BUTTON_TAG = 1;
	private static final int CANCEL_BUTTON_TAG = 2;
	private static final int STOP_BUTTON_TAG = 3;
	
	
	// ===========================================================
	// Fields
	// ===========================================================
	private Button btnStart, btnStop, btnCancel;
	
	private MediaRecorder recorder = new MediaRecorder();
	private boolean isRecording = false;
	private File currentFileRecording;
	private Uri mediaUri = null;
	
	private TextView tvTimer;
	private Handler timerHandler = new Handler();
	private UpdateTimerTask updateTimerTask;
	private long startTime = 0;
	
	private String eventId = "";
	
	// ===========================================================
	// Lifecycle
	// ===========================================================
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		WorkflowLogger.log("AudioEntryActivity - onCreate called");
		setupView();
		eventId = getIntent().getStringExtra(INCIDENT_UUID);
		
		disallowRotation();
		
		if(eventId == null) {
			logger.error("null eventId");
			cancelRecording();
		}
	}
	
	private void disallowRotation() {
		//cannot force this in the AndroidManifest, because we want the user
		//to be able to pick a rotation and stick with it for this activity
		int orientation = getResources().getConfiguration().orientation;
		if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		else {
			//for portrait or square.
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

	// ===========================================================
	// User Interaction
	// ===========================================================
	@Override
	public void onClick(View v) {
		switch ((Integer)v.getTag()) {
		case START_BUTTON_TAG:
			this.startRecording();
			break;
			
		case CANCEL_BUTTON_TAG:
			this.cancelRecording();
			break;
		
		case STOP_BUTTON_TAG:
			this.stopRecording();
			break;
		default:
			// do nothing
		
		}
	}
	
	// ===========================================================
	// UI Setup
	// ===========================================================
	private void setupView() {
		setContentView(R.layout.alert_dialog_audio_entry);
		
		btnStart = (Button)findViewById(R.id.alertDialogAudioEntryStartButton);
		btnStart.setTag(START_BUTTON_TAG);
		btnStart.setOnClickListener(this);
		btnStart.getBackground().setColorFilter(Color.argb(255, 102, 255, 102), PorterDuff.Mode.MULTIPLY);
		
		btnCancel = (Button)findViewById(R.id.alertDialogAudioEntryCancelButton);
		btnCancel.setTag(CANCEL_BUTTON_TAG);
		btnCancel.setOnClickListener(this);
		
		btnStop = (Button)findViewById(R.id.alertDialogAudioEntryStopButton);
		btnStop.setTag(STOP_BUTTON_TAG);
		btnStop.setClickable(false);
		btnStop.setOnClickListener(this);
		btnStop.getBackground().setColorFilter(Color.argb(255, 255, 0, 0), PorterDuff.Mode.MULTIPLY);
		
		tvTimer = (TextView)findViewById(R.id.alertDialogTimerTextView);
	}
	
	// Sets the button attributes to how they should appear before recording.
	public void resetButtonAttributes() {
		btnStop.getBackground().setColorFilter(Color.argb(102, 255, 0, 0), PorterDuff.Mode.MULTIPLY);
		btnStop.setTextColor(Color.argb(102, 255, 255, 255));
		btnStart.getBackground().setColorFilter(Color.argb(255, 102, 255, 102), PorterDuff.Mode.MULTIPLY);
		btnStart.setTextColor(Color.argb(255, 0, 0, 0));
	}
	
	// ===========================================================
	// Audio Controls
	// ===========================================================
	private void setupMediaRecorder() {
		recorder.reset();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
		recorder.setMaxDuration(30*1000);
		recorder.setOnInfoListener(this);
	}
	
	// Resets the media recorder, and begins audio recording.
	private void startRecording() {
		if (isRecording) {
			return;
		}
		
		this.setupMediaRecorder();
		
		// Set the stop button to fully opaque.
		btnStop.getBackground().setColorFilter(Color.argb(255, 255, 0, 0), PorterDuff.Mode.MULTIPLY);
		btnStop.setTextColor(Color.argb(255, 255, 255, 255));
		btnStop.setClickable(true);
		btnStart.getBackground().setColorFilter(Color.argb(102, 102, 255, 102), PorterDuff.Mode.MULTIPLY);
		btnStart.setTextColor(Color.argb(102, 0, 0, 0));
		
		// The filepath we will be saving to should have already been set. If not, set it.
		if (currentFileRecording == null) {
			currentFileRecording = this.fileForSDCardWrite();	
		}
		
		String filepath = currentFileRecording.getAbsolutePath();
		recorder.setOutputFile(filepath);
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		recorder.start();
		WorkflowLogger.log("AudioEntryActivity - started recording audio");
		Toast.makeText(this, "Now recording...", Toast.LENGTH_SHORT).show();
		isRecording = true;

		// Start the timer counter.
		startTimer();
	}
	
	private void startTimer() {
		updateTimerTask = new UpdateTimerTask();
		startTime = System.currentTimeMillis();
		timerHandler.removeCallbacks(updateTimerTask);
		timerHandler.postDelayed(updateTimerTask, 500);
	}
	
	// Remove the file if we were recording then exit the activity.
	private void cancelRecording() {
		if (isRecording) {
			recorder.stop();
			currentFileRecording.delete();
			isRecording = false;
			Toast.makeText(this, "Recording cancelled", Toast.LENGTH_LONG).show();
			WorkflowLogger.log("AudioEntryActivity - cancelled recording audio");
		}
		setResult(Activity.RESULT_CANCELED);
		finish();
		
	}
	
	// Stop recording and write the file info to the incident provider.
	private void stopRecording() {
		if (isRecording) {
			recorder.stop();
			WorkflowLogger.log("AudioEntryActivity - stopped recording");
			WorkflowLogger.log("AudioEntryActivity - writing audio to file: " + currentFileRecording.getAbsolutePath());
			isRecording = false;
			Uri uri = insertAudioEntryIntoIncidentProvider();
			timerHandler.removeCallbacks(updateTimerTask);
			tvTimer.setText("");
			Intent data = new Intent();
			data.setData(uri);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
	}
	
	@Override
	public void finish() {
		super.finish();
		if(isRecording) {
			cancelRecording();
		}
	}
	
	private Uri insertAudioEntryIntoIncidentProvider() {
		// Store the fileUri in the content provider.
		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(MediaTableSchema.EVENT_ID, eventId);
		cv.put(MediaTableSchema.DATA_TYPE, MediaTableSchema.AUDIO_DATA_TYPE);
		cv.put(MediaTableSchema.DATA, currentFileRecording.getAbsolutePath());
		mediaUri = cr.insert(MediaTableSchema.CONTENT_URI, cv);
		logger.debug( "Inserted " + currentFileRecording.getAbsolutePath() + " into " + mediaUri.toString());
		WorkflowLogger.log("AudioEntryActivity - inserted audio with uri: " + mediaUri);
		Toast.makeText(this, "Audio recording saved!", Toast.LENGTH_SHORT).show();
		return mediaUri;
	}
	
	@Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		switch(what) {
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
			this.stopRecording();
			Toast.makeText(this, "Recording limit reached...stopping", Toast.LENGTH_SHORT).show();
			
			break;
		default:
			// do nothing.
		}
	}
	// ***********************************************************

	private class UpdateTimerTask implements Runnable {

		@Override
		public void run() {
			final long start = startTime;
			long millis = System.currentTimeMillis() - start;
			int seconds = (int) (millis / 1000);
			int minutes = seconds / 60;
			seconds = seconds % 60;

			if (seconds < 10) {
				tvTimer.setText("(" + "" + minutes + ":0" + seconds + ")");
			} else {
				tvTimer.setText("(" + "" + minutes + ":" + seconds + ")");
			}

			timerHandler.postDelayed(this, 500);
		}

	}

	/**
	 * Generates a new filename, sets this dialog's file member to that value,
	 * and returns it.
	 */
	@Override
	public File fileForSDCardWrite() {
		File dir = new File(Environment.getExternalStorageDirectory(),DIRECTORY_AUDIO); 
		String filename = String.valueOf(System.currentTimeMillis());
		currentFileRecording = new File(dir, filename + "_audio.3gp");
		return currentFileRecording;
	}
	
}
