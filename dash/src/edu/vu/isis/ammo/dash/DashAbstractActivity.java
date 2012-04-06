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
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import edu.vu.isis.ammo.INetPrefKeys;
import edu.vu.isis.ammo.IntentNames;
import edu.vu.isis.ammo.api.AmmoPreference;
import edu.vu.isis.ammo.api.AmmoRequest;
import edu.vu.isis.ammo.dash.dialogs.PreviewDialog;
import edu.vu.isis.ammo.dash.preferences.DashPreferences;
import edu.vu.isis.ammo.dash.preview.DashPreview;
import edu.vu.isis.ammo.dash.provider.IncidentSchema.EventTableSchema;

/**
 * Based on discussion with Mari March 2011.
 * 
 * The Dash application is partitioned into two sub-applications: Dash and
 * DashTemplate. This abstract activity handles all the business logic and UI
 * pieces shared between the sub-applications.
 */
public abstract class DashAbstractActivity extends Activity {
	private static final Logger logger = LoggerFactory.getLogger("class.AbstractActivity");
	private String id;
	protected DashModel model;

	protected long time = System.currentTimeMillis();
	protected View cameraContainer, audioContainer;
	protected Button removeButton;
	private boolean openForEdit = true;

	public static final String DID_SUBSCRIBE_PREF = "did_subscribe_pref";
	public static final int JPEG_QUALITY = 80;
	public static final String FILE_URI = "fileUri";

	public static final File ROOT_DIR = Environment.getExternalStorageDirectory();
	public static final File CAMERA_DIR = new File(ROOT_DIR, "support/Dash/ammo_camera_activity");
	public static final File IMAGE_DIR = new File(ROOT_DIR, "support/Dash/ammo_image");
	public static final File AUDIO_DIR = new File(ROOT_DIR, "support/Dash/ammo_audio");
	public static final File TEXT_DIR = new File(ROOT_DIR, "support/Dash/ammo_text");
	public static final File VIDEO_DIR = new File(ROOT_DIR, "support/Dash/ammo_video");
	public static final File TEMPLATE_DIR = new File(ROOT_DIR, "support/Dash/ammo_template");

	private static final String BUNDLE_MODEL = "model";
	private static final String BUNDLE_PHOTO_URI = "photo_uri";
	private static final String BUNDLE_MEDIA_URI = "media_uri";
	private static final String BUNDLE_THUMBNAIL = "thumbnail";
	private static final String BUNDLE_MEDIA_TYPE = "media_type";
	private static final String BUNDLE_TEMPLATE_DATA = "template_data";

	public static final int TEXT_TYPE = 1;
	public static final int AUDIO_TYPE = 2;
	public static final int IMAGE_TYPE = 3;
	public static final int VIDEO_TYPE = 4;
	public static final int MAP_TYPE = 5;

	public static final String MODE = "mode";
	public static final String OPEN_FOR_EDIT_EXTRA = "OPEN_FOR_EDIT";

	private static final int MENU_OPTION_PREVIEW = 0;
	private static final int MENU_ABOUT = 1;
	private static final int MENU_REGISTER = 2;
	private static final int MENU_OPTION_SETTINGS = 3;
	public static final int DIALOG_PREVIEW_ENTRY = 0;

	public AmmoRequest.Builder ab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getContentViewResourceId());
		WorkflowLogger.log("DashAbstractActivity - onCreate");
		logger.info("we think we're getting this one...DashAbstractActivity - onCreate");
		//Log.i("DashWorkflow", "DashAbstractActivity - onCreate");
		
		// Test code: make a query on the contacts app for the users unit.
		openForEdit = getIntent().getBooleanExtra(OPEN_FOR_EDIT_EXTRA, true);
		setupView();
		clearAll();

		// only does something if this was started from a shortcut:
		startupMode();

		try {
			Util.setupFilePaths();
		} catch (IOException e) {
			logger.error("::onCreate:  could not setup file paths", e);
			Util.makeToast(this, "could not setup file paths");
			finish();
			return;
		}

		this.ab = AmmoRequest.newBuilder(this);
	}

	@Override
	public void onDestroy() {
		this.ab.releaseInstance();
		super.onDestroy();
	}

	abstract public int getContentViewResourceId();

	@Override
	public void onResume() {
		super.onResume();
		updateButtons();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		toModel();
		bundle.putParcelable(BUNDLE_MODEL, model.getContentValues());
		bundle.putParcelable(BUNDLE_PHOTO_URI, model.getPhotoUri());
		bundle.putParcelable(BUNDLE_MEDIA_URI, model.getCurrentMediaUri());
		bundle.putParcelable(BUNDLE_THUMBNAIL, model.getThumbnail());
		bundle.putInt(BUNDLE_MEDIA_TYPE, model.getCurrentMediaType());
		bundle.putString(BUNDLE_TEMPLATE_DATA, model.getTemplateData());
	}

	@Override
	public void onRestoreInstanceState(Bundle bundle) {
		model.setContentValues(bundle.<ContentValues> getParcelable(BUNDLE_MODEL));
		model.setPhotoUri(bundle.<Uri> getParcelable(BUNDLE_PHOTO_URI));
		model.setCurrentMediaUri(bundle.<Uri> getParcelable(BUNDLE_MEDIA_URI));
		model.setThumbnail(bundle.<Bitmap> getParcelable(BUNDLE_THUMBNAIL));
		model.setCurrentMediaType(bundle.getInt(BUNDLE_MEDIA_TYPE));
		model.setTemplateData(bundle.getString(BUNDLE_TEMPLATE_DATA));
		fromModel();
	}

	/**
	 * Called after setContentView. Don't forget super.setupView();
	 */
	protected void setupView() {
		// Set buttons.

		Button saveButton = (Button) findViewById(R.id.saveButton);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
				clearAll();
				// run the mode activity again! if one exists.
				startupMode();
			}
		});

		cameraContainer = findViewById(R.id.cameraButton);
		cameraContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (model.getCurrentMediaUri() == null) {
					launchCameraActivity();
				}
			}
		});

		audioContainer = findViewById(R.id.audioButton);
		audioContainer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (model.getCurrentMediaUri() == null) {
					launchAudioActivity();
				}
			}
		});

		removeButton = (Button) findViewById(R.id.removeButton);
		removeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeContent();
			}
		});

		((ImageView) findViewById(R.id.mediaPreview)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDialog(DIALOG_PREVIEW_ENTRY);
			}
		});
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
	protected void onPrepareDialog(final int id, Dialog dialog, Bundle bundle) {
		if (!(dialog instanceof PreviewDialog) || model.getCurrentMediaUri() == null) {
			logger.error("Could not prepare dialog:  Bad state.");
			Util.makeToast(this, "Could not find media to display");
			return;
		}

		PreviewDialog pd = (PreviewDialog) dialog;
		String dataPath = MediaActivityManager.getPath(getContentResolver(), model.getCurrentMediaUri());
		if (dataPath == null) {
			logger.error("Could not prepare dialog:  Could not get path for the media.");
			Util.makeToast(this, "Could not get media to display");
			return;
		}
		pd.setDataAndDataType(dataPath, model.getCurrentMediaType());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if the child activity was canceled, ignore.
		if (resultCode != Activity.RESULT_OK) {
			model.setPhotoUri(null);
			// if we started with a weird mode, then we should exit.
			if (getMode() != -1) {
				finish();
			}
			return;
		}

		boolean success = true;
		switch (requestCode) {
		case IMAGE_TYPE:
			// Check whether the file is coming from the stock camera or TRAQ
			// camera.
			// Set the model's photo uri to whatever was passed in.
			Uri fileUri = model.getPhotoUri();
			if (data != null) {
				String traqFilepath = data.getExtras().getString("filename");
				if (traqFilepath != null) {
					// Append the file prefix to the filepath so it can be
					// handled by the pre-existing media logic.
					fileUri = Uri.parse("file://" + traqFilepath);
					WorkflowLogger.log("DashAbstractActivity - received fileUri from Traq camera: " + fileUri.toString());
				}

			}

			model.setPhotoUri(fileUri);
			model.setThumbnail(MediaActivityManager.getThumbnail(fileUri));
			model.setCurrentMediaUri(MediaActivityManager.processPicture(getContentResolver(), data, id, model.getThumbnail()));
			model.setCurrentMediaType(IMAGE_TYPE);
			if (model.getCurrentMediaUri() == null) {
				success = false;
			}
			break;
		case AUDIO_TYPE:
			model.setCurrentMediaUri(MediaActivityManager.processAudio(getContentResolver(), data, id));
			model.setCurrentMediaType(AUDIO_TYPE);
			if (model.getCurrentMediaUri() == null) {
				success = false;
			} else {
				WorkflowLogger.log("DashAbstractActivity - received media table Uri from audio recorder with Uri: " + model.getCurrentMediaUri());
			}
			break;
		}

		updateButtons();

		if (!success) {
			// see the log file for more information.
			Util.makeToast(this, "Error processing the result.");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_OPTION_PREVIEW, Menu.NONE, R.string.option_menu_viewdashes);
		menu.add(0, MENU_ABOUT, 0, R.string.option_menu_about);
		menu.add(0, MENU_OPTION_SETTINGS, Menu.NONE, "Settings");
		// menu.add(0, MENU_REGISTER, Menu.NONE, R.string.option_menu_register);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_OPTION_PREVIEW:
			startActivity(new Intent(this, DashPreview.class));
			break;
		case MENU_ABOUT:
			showAboutScreen();
			break;
		case MENU_REGISTER:
			// Manually register with the core for subscriptions.
			AnnounceReceiver announceReceiver = new AnnounceReceiver();
			announceReceiver.onReceive(this, new Intent(IntentNames.AMMO_READY));
			break;
		case MENU_OPTION_SETTINGS:
			launchSettingsActivity();
			break;
		default:
			// do nothing.
		}

		return true;
	}

	private void showAboutScreen() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	private void launchSettingsActivity() {
		Intent intent = new Intent(this, DashPreferences.class);
		startActivity(intent);
	}

	private void removeContent() {
		if (model.getCurrentMediaUri() != null) {
			removeData(getContentResolver(), model.getCurrentMediaUri());
		}
		model.setCurrentMediaUri(null);
		updateButtons();
	}

	/**
	 * Gets called when we need to update which set of buttons is displayed
	 * on-screen.
	 */
	private void updateButtons() {
		if (model.getCurrentMediaUri() != null) {
			// turn off buttons and enable the remove button
			cameraContainer.setVisibility(View.GONE);
			audioContainer.setVisibility(View.GONE);
			removeButton.setVisibility(getEditVisibility());
			((ImageView) findViewById(R.id.mediaPreview)).setVisibility(View.VISIBLE);
			if (model.getCurrentMediaType() == IMAGE_TYPE) {
				((ImageView) findViewById(R.id.mediaPreview)).setImageBitmap(model.getThumbnail());
				removeButton.setText("Remove picture");
			} else if (model.getCurrentMediaType() == AUDIO_TYPE) {
				((ImageView) findViewById(R.id.mediaPreview)).setImageResource(R.drawable.sound);
				removeButton.setText("Remove audio");
			}
		} else {
			cameraContainer.setVisibility(getEditVisibility());
			audioContainer.setVisibility(getEditVisibility());
			removeButton.setVisibility(View.GONE);
			((ImageView) findViewById(R.id.mediaPreview)).setVisibility(View.GONE);
		}
	}

	protected int getEditVisibility() {
		return isOpenForEdit() ? View.VISIBLE : View.GONE;
	}

	protected boolean isOpenForEdit() {
		return openForEdit;
	}

	private void launchCameraActivity() {
		WorkflowLogger.log("DashAbstractActivity - attempting to start camera activity...");
		model.setPhotoUri(MediaActivityManager.createPhotoUri(this));
		if (model.getPhotoUri() == null) {
			logger.error("::launchCameraActivity:  No photo URI.");
			Util.makeToast(this, "Could not start Camera:  could not create file");
			return;
		}
		try {
			WorkflowLogger.log("DashAbstractActivity - starting camera activity");
			startActivityForResult(MediaActivityManager.createPictureIntent(this, id, model.getPhotoUri()), IMAGE_TYPE);
		} catch (ActivityNotFoundException e) {
			logger.error("::launchCameraActivity:  Activity not found.", e);
			Util.makeToast(this, "Could not start Camera:  not found");
		}
	}

	private void launchAudioActivity() {
		try {
			WorkflowLogger.log("DashAbstractActivity - starting audio recorder activity");
			startActivityForResult(MediaActivityManager.createAudioIntent(this, id), AUDIO_TYPE);
		} catch (ActivityNotFoundException e) {
			logger.error("::launchAudioActivity:  Activity not found.", e);
			Util.makeToast(this, "Could not start Audio recording:  not found");
		}
	}

	/**
	 * Stores the current state of the event into the ContentProvider. The
	 * disposition is changed from _DRAFT to _SEND. In other words, this
	 * function will mark a report as ready for distribution and create a new
	 * one for editing (middleware distribution is not triggered by calling this
	 * method).
	 */
	private void save() {
		toModel();
		final ContentResolver resolver = getContentResolver();
		
		final Uri incidentUri = resolver.insert(EventTableSchema.CONTENT_URI, model.getContentValues());
		WorkflowLogger.log("DashAbstractActivity - inserted Dash event into EventTable with Uri: " + incidentUri);
		// Post the event for dispatch.

		try {
			this.ab
			    .provider(incidentUri)
			    .topicFromProvider()
			    .post();
			WorkflowLogger.log("DashAbstractActivity - posted event to AmmoCore with Uri: " + incidentUri);
		} catch (RemoteException ex) {
			logger.error("post incident failed {}", ex.getStackTrace());
		}
		if (model.getCurrentMediaUri() != null) {
			try {
				this.ab
				    .provider(model.getCurrentMediaUri())
				    .topicFromProvider()
				    .post();
				WorkflowLogger.log("DashAbstractActivity - posted media to AmmoCore with Uri: " + model.getCurrentMediaUri());
			} catch (RemoteException ex) {
				logger.error("post media failed {}", ex.getStackTrace());
			}
		}
		if (model.getTemplateData() != null) {
			
			Uri templateUri = MediaActivityManager.saveTemplateData(resolver, id, model.getTemplateData());
			if (templateUri != null) {
				try {
					this.ab
					    .provider(templateUri)
					    .topicFromProvider()
					    .post();
					WorkflowLogger.log("DashAbstractActivity - posted template to AmmoCore with Uri: " + templateUri);
				} catch (RemoteException ex) {
					logger.error("post template failed {}", ex.getStackTrace());
				}
			}
		}

		Util.makeToast(this, "Report Posted!");
	}

	/**
	 * Clear the screen. Update the model's id.
	 */
	protected void clearAll() {
		model = new DashModel(this);
		id = UUID.randomUUID().toString();
		updateButtons();
	}

	/**
	 * Sets certain values in the model that should always be present.
	 */
	protected void toModel() {
		model.setId(id);
		model.setTime(time);
        final String operatorId = AmmoPreference
               .getInstance(this)
               .getString(INetPrefKeys.CORE_OPERATOR_ID, 
                          INetPrefKeys.DEFAULT_CORE_OPERATOR_ID);
	
		model.setOriginator(operatorId);
	}

	protected void fromModel() {
		if (model.getId() != null) {
			id = model.getId();
		}
		if (model.getTime() != null) {
			time = model.getTime();
		}
	}

	/**
	 * @return true on success, false on failure
	 */
	private static boolean removeData(ContentResolver contentResolver, Uri uri) {
		if (uri == null || contentResolver.delete(uri, null, null) != 1) {
			logger.error("could not remove uri: " + uri);
			return false;
		} else {
			WorkflowLogger.log("DashAbstractActivity - removed data with Uri: " + uri);
		}
		return true;
	}

	// only does something if this was started from a shortcut:
	private void startupMode() {
		switch (getMode()) {
		case IMAGE_TYPE:
			launchCameraActivity();
			break;
		case AUDIO_TYPE:
			launchAudioActivity();
			break;
		}
	}

	/**
	 * @return -1 on when in normal mode. or one of IMAGE_TYPE, AUDIO_TYPE.
	 */
	private int getMode() {
		return getIntent().getIntExtra(MODE, -1);
	}
}
