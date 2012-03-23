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

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import edu.vu.isis.ammo.dash.template.view.LocationView;

/**
 * Dash activity that can generate reports consisting of a comment, location, timestamp, and a single
 * piece of media.
 * @author demetri
 *
 * @author phreed
 * Transcription is incomplete : marked with TSI
 */
public class Dash extends DashAbstractActivity {
	private EditText descriptionText;
	private EditText timeText;
	private LocationView locationView;
	

	
	//TSI // private View trascribeContainer;
	/* begin: For IBM transcription */
	public static Intent easr_service;
	public static Socket socket_ASR;
	public static PrintWriter out_ASR;
	public static BufferedReader in_ASR;
	//TSI // private final int SERVERPORT_EASR= 2646;
	//TSI // private final String SERVERIP = "127.0.0.1";
	//TSI // private boolean recording;
	/* end: For IBM transcription */
	//TSI // private TextView transcribeText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//TSI // recording = false;
/*  TSI // commented out for now
		easr_service = new Intent();
                easr_service.setClassName("com.ibm.asr", "com.ibm.asr.EAsr");
                startService(easr_service);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			WorkflowLogger.log("Dash - Exception: Unable To Sleep 5 Seconds After Launch: " + e.getMessage() );
		}
		 transcribeText = (TextView)findViewById(R.id.transcriptionText);
		 */
	}


	@Override
	public int getContentViewResourceId() {
		return R.layout.dash;
	}
	
	protected void setupView() {
		super.setupView();
		WorkflowLogger.log("Dash - setting up freeform Dash");
		timeText = (EditText)findViewById(R.id.time_text);
		ImageButton timeButton = (ImageButton)findViewById(R.id.timeButton);
		timeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateTime(System.currentTimeMillis());
			}
		});
		
		locationView = new LocationView(this, null);
		descriptionText = (EditText)findViewById(R.id.description);
		

		// Set the in the edit text manually right here since the model object hasn't necessarily been created yet.
		// (If model isn't created, updateTime() will fail).
		timeText.setText(Util.formatTime(time));

	/*	transcribePic = (ImageView)findViewById(R.id.transcribePic);

		trascribeContainer = findViewById(R.id.transcriptionButton);
		descriptionText.append("" + trascribeContainer.getHeight());
		
		trascribeContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			
				if (recording == false){
				      try{
	    			             socket_ASR = new Socket(SERVERIP, SERVERPORT_EASR);
                   			     out_ASR= new PrintWriter( new BufferedWriter(new OutputStreamWriter(socket_ASR.getOutputStream()), 4096),true);
    		                             in_ASR= new BufferedReader(new InputStreamReader(socket_ASR.getInputStream()), 4096);
	    	                             out_ASR.println("EN Micon /sdcard/test.wav");
		                             socket_ASR.close();
				      
					     descriptionText.setPressed(true);
					     descriptionText.setBackgroundColor(Color.YELLOW);
		                             recording = true;
				           //  transcribePic.setImageResource(R.drawable.trascription_button);
					     //trascribeContainer.setImageResource(R.drawable.trascription_end_button);
    			              }catch(Exception e){
					     WorkflowLogger.log("Dash - Exception: Starting Transcription: " + e.getMessage() );
			              }
				      try {					   
					     transcribeText.setText("Speak");
					     Thread.sleep(1000);
				      } catch (InterruptedException e) {				
					     WorkflowLogger.log("Dash - Exception: Unable To Sleep 1 Second After Starting: " + e.getMessage() );
				      }
				}
				else
				{
					transcribeText.setText("Processing");
				//	ProgressDialog dialog = new ProgressDialog(transcribeText.getContext());
				//	dialog.setMessage("Processing...");
				//	dialog.setIndeterminate(true);
				//	dialog.setCancelable(false);
				//	dialog.show();
					try{    // stop recording
						socket_ASR = new Socket(SERVERIP, SERVERPORT_EASR);
						out_ASR= new PrintWriter( new BufferedWriter(new OutputStreamWriter(socket_ASR.getOutputStream()), 4096),true);
						in_ASR= new BufferedReader(new InputStreamReader(socket_ASR.getInputStream()), 4096);
						out_ASR.println("EN Micoff");

						// get text
						String output = in_ASR.readLine();
						// write text to screen
						descriptionText.append(output);
						descriptionText.append("\n\n");
						//transcribePic.setImageResource(R.drawable.trascription_end_button);
						descriptionText.setBackgroundColor(Color.WHITE);
						recording = false;
						//descriptionText.setPressed(false); 
						
						//trascribeContainer.setImageResource(R.drawable.trascription_button);
					}catch(Exception e){
						WorkflowLogger.log("Dash - Exception: Stopping Transcription: " + e.getMessage() );
				    }
				//	try {

//					ProgressDialog dialog = new ProgressDialog(this);
//dialog.setMessage("Thinking...");
//dialog.setIndeterminate(true);
//dialog.setCancelable(false);
//dialog.show();
				//	      Thread.sleep(5000);
//dialog.
				//	} catch (InterruptedException e) {				
				//	      WorkflowLogger.log("Dash - Exception: Unable To Sleep 5 Seconds After Stopping: " + e.getMessage() );
				//	}
				//	dialog.dismiss();
					transcribeText.setText("Dictate");
				}
			}
		});
*/
	}
	
	@Override
	public void onResume() {
		super.onResume();
		locationView.resume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		locationView.pause();
	}
	
	@Override
	protected void toModel() {
		super.toModel();
		model.setDescription(descriptionText.getText().toString());
		model.setLocation(locationView.getLocation());
	}
	
	@Override
	protected void fromModel() {
		super.fromModel();
		if(model.getDescription()!=null) {
			descriptionText.setText(model.getDescription());
		}
		if(model.getLocation()!=null) {
			locationView.setLocation(model.getLocation());
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode != Activity.RESULT_OK) {
			//if the child activity was canceled, ignore.
			return;
		}
		
		if(requestCode != MAP_TYPE) {
			//wasn't for me
			return;
		}
		
		if(!locationView.processMapPoint(data)) {
			//see the log file for more information.
			Util.makeToast(this, "Error processing the result.");
		}
	}
	
	/**
	 * Update the time member variable, its value in the model, and also the text field.
	 * @param time The new time to be stored.
	 */
	private void updateTime(long time) {
		super.time = time;
		WorkflowLogger.log("Dash - updated Dash event timestamp to time: " + time);
		model.setTime(time);
		timeText.setText(Util.formatTime(time));
	}
}