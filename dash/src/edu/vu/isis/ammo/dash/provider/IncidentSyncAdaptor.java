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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SyncStateContract.Constants;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import edu.vu.isis.ammo.dash.provider.IncidentSchemaBase.CategoryTableSchemaBase;
import edu.vu.isis.ammo.dash.provider.IncidentSchemaBase.EventTableSchemaBase;
import edu.vu.isis.ammo.dash.provider.IncidentSchemaBase.MediaTableSchemaBase;

/**
 * SyncAdapter implementation for syncing sample SyncAdapter contacts to the
 * platform ContactOperations provider.
 */
public class IncidentSyncAdaptor extends AbstractThreadedSyncAdapter {
	private final Logger logger = LoggerFactory.getLogger("class.IncidentSyncAdaptor");

	private final AccountManager accountManager;
	@SuppressWarnings("unused")
	private final Context context;
	@SuppressWarnings("unused")
	private Date lastUpdate;

	public IncidentSyncAdaptor(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		this.context = context;
		accountManager = AccountManager.get(context);
		this.lastUpdate = new Date();
	}

	protected static final UriMatcher uriMatcher;

	protected static String[] mediaProjectionKey;
	protected static HashMap<String, String> mediaProjectionMap;

	protected static String[] eventProjectionKey;
	protected static HashMap<String, String> eventProjectionMap;

	protected static String[] categoryProjectionKey;
	protected static HashMap<String, String> categoryProjectionMap;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		//uriMatcher.addURI(IncidentSchemaBase.AUTHORITY, Tables.MEDIA_TBL, MEDIA_SET);
		//uriMatcher.addURI(IncidentSchemaBase.AUTHORITY, Tables.MEDIA_TBL + "/#", MEDIA_ID);

		//uriMatcher.addURI(IncidentSchemaBase.AUTHORITY, Tables.EVENT_TBL, EVENT_SET);
		//uriMatcher.addURI(IncidentSchemaBase.AUTHORITY, Tables.EVENT_TBL + "/#", EVENT_ID);

		//uriMatcher.addURI(IncidentSchemaBase.AUTHORITY, Tables.CATEGORY_TBL, CATEGORY_SET);
		//uriMatcher.addURI(IncidentSchemaBase.AUTHORITY, Tables.CATEGORY_TBL + "/#", CATEGORY_ID);


		HashMap<String, String> columns;
		mediaProjectionKey = new String[1];
		mediaProjectionKey[0] = MediaTableSchemaBase._ID;

		columns = new HashMap<String, String>();
		columns.put(MediaTableSchemaBase._ID, MediaTableSchemaBase._ID);
		columns.put(MediaTableSchemaBase.EVENT_ID, "\""+MediaTableSchemaBase.EVENT_ID+"\""); 
		columns.put(MediaTableSchemaBase.DATA_TYPE, "\""+MediaTableSchemaBase.DATA_TYPE+"\"");
		columns.put(MediaTableSchemaBase.DATA, "\""+MediaTableSchemaBase.DATA+"\""); 
		columns.put(MediaTableSchemaBase.CREATED_DATE, "\""+MediaTableSchemaBase.CREATED_DATE+"\""); 
		columns.put(MediaTableSchemaBase.MODIFIED_DATE, "\""+MediaTableSchemaBase.MODIFIED_DATE+"\""); 
		columns.put(MediaTableSchemaBase._RECEIVED_DATE, "\""+MediaTableSchemaBase._RECEIVED_DATE+"\"");
		columns.put(MediaTableSchemaBase._DISPOSITION, "\""+MediaTableSchemaBase._DISPOSITION+"\"");

		mediaProjectionMap = columns;

		eventProjectionKey = new String[1];
		eventProjectionKey[0] = EventTableSchemaBase._ID;

		columns = new HashMap<String, String>();
		columns.put(EventTableSchemaBase._ID, EventTableSchemaBase._ID);
		columns.put(EventTableSchemaBase.UUID, "\""+EventTableSchemaBase.UUID+"\""); 
		columns.put(EventTableSchemaBase.MEDIA_COUNT, "\""+EventTableSchemaBase.MEDIA_COUNT+"\""); 
		columns.put(EventTableSchemaBase.ORIGINATOR, "\""+EventTableSchemaBase.ORIGINATOR+"\""); 
		columns.put(EventTableSchemaBase.DISPLAY_NAME, "\""+EventTableSchemaBase.DISPLAY_NAME+"\""); 
		columns.put(EventTableSchemaBase.CATEGORY_ID, "\""+EventTableSchemaBase.CATEGORY_ID+"\""); 
		columns.put(EventTableSchemaBase.TITLE, "\""+EventTableSchemaBase.TITLE+"\""); 
		columns.put(EventTableSchemaBase.DESCRIPTION, "\""+EventTableSchemaBase.DESCRIPTION+"\""); 
		columns.put(EventTableSchemaBase.LONGITUDE, "\""+EventTableSchemaBase.LONGITUDE+"\""); 
		columns.put(EventTableSchemaBase.LATITUDE, "\""+EventTableSchemaBase.LATITUDE+"\""); 
		columns.put(EventTableSchemaBase.CREATED_DATE, "\""+EventTableSchemaBase.CREATED_DATE+"\""); 
		columns.put(EventTableSchemaBase.MODIFIED_DATE, "\""+EventTableSchemaBase.MODIFIED_DATE+"\""); 
		columns.put(EventTableSchemaBase.CID, "\""+EventTableSchemaBase.CID+"\""); 
		columns.put(EventTableSchemaBase.CATEGORY, "\""+EventTableSchemaBase.CATEGORY+"\""); 
		columns.put(EventTableSchemaBase.UNIT, "\""+EventTableSchemaBase.UNIT+"\""); 
		columns.put(EventTableSchemaBase.SIZE, "\""+EventTableSchemaBase.SIZE+"\""); 
		columns.put(EventTableSchemaBase.DEST_GROUP_TYPE, "\""+EventTableSchemaBase.DEST_GROUP_TYPE+"\""); 
		columns.put(EventTableSchemaBase.DEST_GROUP_NAME, "\""+EventTableSchemaBase.DEST_GROUP_NAME+"\""); 
		columns.put(EventTableSchemaBase.STATUS, "\""+EventTableSchemaBase.STATUS+"\""); 
		columns.put(EventTableSchemaBase._RECEIVED_DATE, "\""+EventTableSchemaBase._RECEIVED_DATE+"\"");
		columns.put(EventTableSchemaBase._DISPOSITION, "\""+EventTableSchemaBase._DISPOSITION+"\"");

		eventProjectionMap = columns;

		categoryProjectionKey = new String[1];
		categoryProjectionKey[0] = CategoryTableSchemaBase._ID;

		columns = new HashMap<String, String>();
		columns.put(CategoryTableSchemaBase._ID, CategoryTableSchemaBase._ID);
		columns.put(CategoryTableSchemaBase.MAIN_CATEGORY, "\""+CategoryTableSchemaBase.MAIN_CATEGORY+"\""); 
		columns.put(CategoryTableSchemaBase.SUB_CATEGORY, "\""+CategoryTableSchemaBase.SUB_CATEGORY+"\""); 
		columns.put(CategoryTableSchemaBase.TIGR_ID, "\""+CategoryTableSchemaBase.TIGR_ID+"\""); 
		columns.put(CategoryTableSchemaBase.ICON_TYPE, "\""+CategoryTableSchemaBase.ICON_TYPE+"\"");
		columns.put(CategoryTableSchemaBase.ICON, "\""+CategoryTableSchemaBase.ICON+"\""); 
		columns.put(CategoryTableSchemaBase._RECEIVED_DATE, "\""+CategoryTableSchemaBase._RECEIVED_DATE+"\"");
		columns.put(CategoryTableSchemaBase._DISPOSITION, "\""+CategoryTableSchemaBase._DISPOSITION+"\"");

		categoryProjectionMap = columns;

	}

	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		try {
			// use the account manager to request the credentials
			final String  authtoken = accountManager
					.blockingGetAuthToken(account, Constants.ACCOUNT_TYPE, true);         

			// update the last synced date.
			this.lastUpdate = new Date();
			// update platform contacts.
			logger.debug("Calling contactManager's sync contacts {}", authtoken);
			// fetch and update status messages for all the synced users.
		} catch (final AuthenticatorException ex) {
			syncResult.stats.numParseExceptions++;
			logger.error("AuthenticatorException", ex);
		} catch (final OperationCanceledException ex) {
			logger.error("OperationCanceledExcetpion", ex);
		} catch (final IOException ex) {
			logger.error("IOException", ex);
			syncResult.stats.numIoExceptions++;
		} catch (final ParseException ex) {
			syncResult.stats.numParseExceptions++;
			logger.error("ParseException", ex);
		} 
	}

	/**
	 * Table Name: media <P>
	 */
	static public class MediaWrapper {
		public MediaWrapper() {
			// logger.info("building MediaWrapper");
		}
		private String eventId;
		public String getEventId() {
			return this.eventId;
		}
		public MediaWrapper setEventId(String val) {
			this.eventId = val;
			return this;
		} 
		private String dataType;
		public String getDataType() { 
			return this.dataType;
		}
		public MediaWrapper setDataType(String val) {
			this.dataType = val;
			return this;
		}

		private String data;
		public String getData() {
			return this.data;
		}
		public MediaWrapper setData(String val) {
			this.data = val;
			return this;
		}  
		private long createdDate;
		public long getCreatedDate() {
			return this.createdDate;
		}
		public MediaWrapper setCreatedDate(long val) {
			this.createdDate = val;
			return this;
		} 
		private long modifiedDate;
		public long getModifiedDate() {
			return this.modifiedDate;
		}
		public MediaWrapper setModifiedDate(long val) {
			this.modifiedDate = val;
			return this;
		} 
		private int _disposition;
		public int get_Disposition() {
			return this._disposition;
		}
		public MediaWrapper set_Disposition(int val) {
			this._disposition = val;
			return this;
		}
		private long _received_date;
		public long get_ReceivedDate() {
			return this._received_date;
		}
		public MediaWrapper set_ReceivedDate(long val) {
			this._received_date = val;
			return this;
		}
	} 
	/**
	 * Table Name: event <P>
	 */
	static public class EventWrapper {
		public EventWrapper() {
			// logger.info("building EventWrapper");
		}
		private String uuid;
		public String getUuid() {
			return this.uuid;
		}
		public EventWrapper setUuid(String val) {
			this.uuid = val;
			return this;
		} 
		private int mediaCount;
		public int getMediaCount() {
			return this.mediaCount;
		}
		public EventWrapper setMediaCount(int val) {
			this.mediaCount = val;
			return this;
		} 
		private String originator;
		public String getOriginator() {
			return this.originator;
		}
		public EventWrapper setOriginator(String val) {
			this.originator = val;
			return this;
		} 
		private String displayName;
		public String getDisplayName() {
			return this.displayName;
		}
		public EventWrapper setDisplayName(String val) {
			this.displayName = val;
			return this;
		} 
		private String categoryId;
		public String getCategoryId() {
			return this.categoryId;
		}
		public EventWrapper setCategoryId(String val) {
			this.categoryId = val;
			return this;
		} 
		private String title;
		public String getTitle() {
			return this.title;
		}
		public EventWrapper setTitle(String val) {
			this.title = val;
			return this;
		} 
		private String description;
		public String getDescription() {
			return this.description;
		}
		public EventWrapper setDescription(String val) {
			this.description = val;
			return this;
		} 
		private double longitude;
		public double getLongitude() {
			return this.longitude;
		}
		public EventWrapper setLongitude(double val) {
			this.longitude = val;
			return this;
		} 
		private double latitude;
		public double getLatitude() {
			return this.latitude;
		}
		public EventWrapper setLatitude(double val) {
			this.latitude = val;
			return this;
		} 
		private long createdDate;
		public long getCreatedDate() {
			return this.createdDate;
		}
		public EventWrapper setCreatedDate(long val) {
			this.createdDate = val;
			return this;
		} 
		private long modifiedDate;
		public long getModifiedDate() {
			return this.modifiedDate;
		}
		public EventWrapper setModifiedDate(long val) {
			this.modifiedDate = val;
			return this;
		} 
		private String cid;
		public String getCid() {
			return this.cid;
		}
		public EventWrapper setCid(String val) {
			this.cid = val;
			return this;
		} 
		private String category;
		public String getCategory() {
			return this.category;
		}
		public EventWrapper setCategory(String val) {
			this.category = val;
			return this;
		} 
		private String unit;
		public String getUnit() {
			return this.unit;
		}
		public EventWrapper setUnit(String val) {
			this.unit = val;
			return this;
		} 
		private long size;
		public long getSize() {
			return this.size;
		}
		public EventWrapper setSize(long val) {
			this.size = val;
			return this;
		} 
		private String destGroupType;
		public String getDestGroupType() {
			return this.destGroupType;
		}
		public EventWrapper setDestGroupType(String val) {
			this.destGroupType = val;
			return this;
		} 
		private String destGroupName;
		public String getDestGroupName() {
			return this.destGroupName;
		}
		public EventWrapper setDestGroupName(String val) {
			this.destGroupName = val;
			return this;
		} 
		private int status;
		public int getStatus() {
			return this.status;
		}
		public EventWrapper setStatus(int val) {
			this.status = val;
			return this;
		} 
		private int _disposition;
		public int get_Disposition() {
			return this._disposition;
		}
		public EventWrapper set_Disposition(int val) {
			this._disposition = val;
			return this;
		}
		private long _received_date;
		public long get_ReceivedDate() {
			return this._received_date;
		}
		public EventWrapper set_ReceivedDate(long val) {
			this._received_date = val;
			return this;
		}
	} 
	/**
	 * Table Name: category <P>
	 */
	static public class CategoryWrapper {
		public CategoryWrapper() {
			// logger.info("building CategoryWrapper");
		}
		private String mainCategory;
		public String getMainCategory() {
			return this.mainCategory;
		}
		public CategoryWrapper setMainCategory(String val) {
			this.mainCategory = val;
			return this;
		} 
		private String subCategory;
		public String getSubCategory() {
			return this.subCategory;
		}
		public CategoryWrapper setSubCategory(String val) {
			this.subCategory = val;
			return this;
		} 
		private String tigrId;
		public String getTigrId() {
			return this.tigrId;
		}
		public CategoryWrapper setTigrId(String val) {
			this.tigrId = val;
			return this;
		} 
		private String iconType;
		public String getIconType() { 
			return this.iconType;
		}
		public CategoryWrapper setIconType(String val) {
			this.iconType = val;
			return this;
		}

		private String icon;
		public String getIcon() {
			return this.icon;
		}
		public CategoryWrapper setIcon(String val) {
			this.icon = val;
			return this;
		}  
		private int _disposition;
		public int get_Disposition() {
			return this._disposition;
		}
		public CategoryWrapper set_Disposition(int val) {
			this._disposition = val;
			return this;
		}
		private long _received_date;
		public long get_ReceivedDate() {
			return this._received_date;
		}
		public CategoryWrapper set_ReceivedDate(long val) {
			this._received_date = val;
			return this;
		}
	} 



	/**
	 * This method is provided with the express purpose of being overridden and extended.
	 *
	 *    StringBuilder sb = new StringBuilder();
	 *    sb.append("\""+MediaTableSchemaBase.FUNCTION_CODE+"\" = '"+ wrap.getFunctionCode()+"'"); 
	 *    return sb.toString();   
	 *
	 * @param wrap
	 */
	protected String mediaSelectKeyClause(MediaWrapper wrap) {
		return null;
	}

	/**
	 * This method is provided with the express purpose of being overridden and extended.
	 * @param wrap
	 */
	protected ContentValues mediaComposeValues(MediaWrapper wrap) {
		ContentValues cv = new ContentValues();
		cv.put(MediaTableSchemaBase.EVENT_ID, wrap.getEventId()); 
		cv.put(MediaTableSchemaBase.DATA, wrap.getData());
		cv.put(MediaTableSchemaBase.DATA_TYPE, wrap.getDataType()); 
		cv.put(MediaTableSchemaBase.CREATED_DATE, wrap.getCreatedDate()); 
		cv.put(MediaTableSchemaBase.MODIFIED_DATE, wrap.getModifiedDate()); 
		cv.put(MediaTableSchemaBase._RECEIVED_DATE, wrap.get_ReceivedDate());
		cv.put(MediaTableSchemaBase._DISPOSITION, wrap.get_Disposition());
		return cv;   
	}



	/**
	 * This method is provided with the express purpose of being overridden and extended.
	 *
	 *    StringBuilder sb = new StringBuilder();
	 *    sb.append("\""+EventTableSchemaBase.FUNCTION_CODE+"\" = '"+ wrap.getFunctionCode()+"'"); 
	 *    return sb.toString();   
	 *
	 * @param wrap
	 */
	protected String eventSelectKeyClause(EventWrapper wrap) {
		return null;
	}

	/**
	 * This method is provided with the express purpose of being overridden and extended.
	 * @param wrap
	 */
	protected ContentValues eventComposeValues(EventWrapper wrap) {
		ContentValues cv = new ContentValues();
		cv.put(EventTableSchemaBase.UUID, wrap.getUuid()); 
		cv.put(EventTableSchemaBase.MEDIA_COUNT, wrap.getMediaCount()); 
		cv.put(EventTableSchemaBase.ORIGINATOR, wrap.getOriginator()); 
		cv.put(EventTableSchemaBase.DISPLAY_NAME, wrap.getDisplayName()); 
		cv.put(EventTableSchemaBase.CATEGORY_ID, wrap.getCategoryId()); 
		cv.put(EventTableSchemaBase.TITLE, wrap.getTitle()); 
		cv.put(EventTableSchemaBase.DESCRIPTION, wrap.getDescription()); 
		cv.put(EventTableSchemaBase.LONGITUDE, wrap.getLongitude()); 
		cv.put(EventTableSchemaBase.LATITUDE, wrap.getLatitude()); 
		cv.put(EventTableSchemaBase.CREATED_DATE, wrap.getCreatedDate()); 
		cv.put(EventTableSchemaBase.MODIFIED_DATE, wrap.getModifiedDate()); 
		cv.put(EventTableSchemaBase.CID, wrap.getCid()); 
		cv.put(EventTableSchemaBase.CATEGORY, wrap.getCategory()); 
		cv.put(EventTableSchemaBase.UNIT, wrap.getUnit()); 
		cv.put(EventTableSchemaBase.SIZE, wrap.getSize()); 
		cv.put(EventTableSchemaBase.DEST_GROUP_TYPE, wrap.getDestGroupType()); 
		cv.put(EventTableSchemaBase.DEST_GROUP_NAME, wrap.getDestGroupName()); 
		cv.put(EventTableSchemaBase.STATUS, wrap.getStatus()); 
		cv.put(EventTableSchemaBase._RECEIVED_DATE, wrap.get_ReceivedDate());
		cv.put(EventTableSchemaBase._DISPOSITION, wrap.get_Disposition());
		return cv;   
	}



	/**
	 * This method is provided with the express purpose of being overridden and extended.
	 *
	 *    StringBuilder sb = new StringBuilder();
	 *    sb.append("\""+CategoryTableSchemaBase.FUNCTION_CODE+"\" = '"+ wrap.getFunctionCode()+"'"); 
	 *    return sb.toString();   
	 *
	 * @param wrap
	 */
	protected String categorySelectKeyClause(CategoryWrapper wrap) {
		return null;
	}

	/**
	 * This method is provided with the express purpose of being overridden and extended.
	 * @param wrap
	 */
	protected ContentValues categoryComposeValues(CategoryWrapper wrap) {
		ContentValues cv = new ContentValues();
		cv.put(CategoryTableSchemaBase.MAIN_CATEGORY, wrap.getMainCategory()); 
		cv.put(CategoryTableSchemaBase.SUB_CATEGORY, wrap.getSubCategory()); 
		cv.put(CategoryTableSchemaBase.TIGR_ID, wrap.getTigrId()); 
		cv.put(CategoryTableSchemaBase.ICON, wrap.getIcon());
		cv.put(CategoryTableSchemaBase.ICON_TYPE, wrap.getIconType()); 
		cv.put(CategoryTableSchemaBase._RECEIVED_DATE, wrap.get_ReceivedDate());
		cv.put(CategoryTableSchemaBase._DISPOSITION, wrap.get_Disposition());
		return cv;   
	}



	interface IMyWriter {
		public long meta(StringBuilder sb);
		public long payload(long rowId, String label, byte[] buf);
	}

	static final int READING_META = 0;
	static final int READING_LABEL = 1;
	static final int READING_PAYLOAD_SIZE = 2;
	static final int READING_PAYLOAD = 3;
	static final int READING_PAYLOAD_CHECK = 4;

	protected long deserializer(File file, IMyWriter writer) {
		logger.debug("::deserializer");
		InputStream ins;
		try {
			ins = new FileInputStream(file);
		} catch (FileNotFoundException e1) {
			return -1;
		}
		BufferedInputStream bufferedInput = new BufferedInputStream(ins);
		byte[] buffer = new byte[1024];
		StringBuilder sb = new StringBuilder();
		long rowId = -1;
		String label = "";
		byte[] payloadSizeBuf = new byte[4];
		int payloadSize = 0;
		byte[] payloadBuf = null;
		int payloadPos = 0;
		try {
			int bytesBuffered = bufferedInput.read(buffer);
			int bufferPos = 0;
			int state = READING_META;
			boolean eod = false;
			while (bytesBuffered > -1) {
				if (bytesBuffered == bufferPos) { 
					bytesBuffered = bufferedInput.read(buffer);
					bufferPos = 0; // reset buffer position
				}
				if (bytesBuffered < 0) eod = true;

				switch (state) {
				case READING_META:
					if (eod) {
						writer.meta(sb);
						break;
					}
					for (; bytesBuffered > bufferPos; bufferPos++) {
						byte b = buffer[bufferPos];
						if (b == '\0') {
							bufferPos++;
							state = READING_LABEL;
							rowId = writer.meta(sb);
							sb = new StringBuilder();
							break;
						}
						sb.append((char)b);
					}
					break;
				case READING_LABEL:
					if (eod)  break;

					for (; bytesBuffered > bufferPos; bufferPos++) {
						byte b = buffer[bufferPos];
						if (b == '\0') {
							label = sb.toString();
							bufferPos++;
							state = READING_PAYLOAD_SIZE;
							payloadPos = 0;
							break;
						}
						sb.append((char)b);
					}
					break;
				case READING_PAYLOAD_SIZE:
					if ((bytesBuffered - bufferPos) < (payloadSizeBuf.length - payloadPos)) { 
						// buffer doesn't contain the last byte of the length
						for (; bytesBuffered > bufferPos; bufferPos++, payloadPos++) { 
							payloadSizeBuf[payloadPos] = buffer[bufferPos];
						}
					} else {
						// buffer contains the last byte of the length
						for (; payloadSizeBuf.length > payloadPos; bufferPos++, payloadPos++) { 
							payloadSizeBuf[payloadPos] = buffer[bufferPos];
						}
						ByteBuffer dataSizeBuf = ByteBuffer.wrap(payloadSizeBuf);
						dataSizeBuf.order(ByteOrder.LITTLE_ENDIAN);
						payloadSize = dataSizeBuf.getInt();
						payloadBuf = new byte[payloadSize];
						payloadPos = 0;
						state = READING_PAYLOAD;
					}
					break;
				case READING_PAYLOAD:
					if ((bytesBuffered - bufferPos) < (payloadSize - payloadPos)) { 
						for (; bytesBuffered > bufferPos; bufferPos++, payloadPos++) { 
							payloadBuf[payloadPos] = buffer[bufferPos];
						}
					} else {
						for (; payloadSize > payloadPos; bufferPos++, payloadPos++) { 
							payloadBuf[payloadPos] = buffer[bufferPos];
						}

						payloadPos = 0;
						state = READING_PAYLOAD_CHECK;
					}
					break;
				case READING_PAYLOAD_CHECK:
					if ((bytesBuffered - bufferPos) < (payloadSizeBuf.length - payloadPos)) { 
						for (; bytesBuffered > bufferPos; bufferPos++, payloadPos++) { 
							payloadSizeBuf[payloadPos] = buffer[bufferPos];
						}
					} else {
						for (; payloadSizeBuf.length > payloadPos; bufferPos++, payloadPos++) { 
							payloadSizeBuf[payloadPos] = buffer[bufferPos];
						}
						ByteBuffer dataSizeBuf = ByteBuffer.wrap(payloadSizeBuf);
						dataSizeBuf.order(ByteOrder.LITTLE_ENDIAN);
						if (payloadSize != dataSizeBuf.getInt()) {
							logger.error("message garbled {} {}", payloadSize, dataSizeBuf.getInt());
							state = READING_LABEL;
							break;
						} 
						writer.payload(rowId, label, payloadBuf);
						state = READING_LABEL;
					}
					break;
				}
			}
			bufferedInput.close();
		} catch (IOException e) {
			logger.error("could not read serialized file");
			return -1;
		}
		return rowId;
	}

	//@Override 
	public ArrayList<File> mediaSerialize(Cursor cursor) {
		logger.debug( "::mediaSerialize");
		ArrayList<File> paths = new ArrayList<File>();      
		if (1 > cursor.getCount()) return paths;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream eos = new DataOutputStream(baos);

		for (boolean more = cursor.moveToFirst(); more; more = cursor.moveToNext()) {
			MediaWrapper iw = new MediaWrapper();
			iw.setEventId(cursor.getString(cursor.getColumnIndex(MediaTableSchemaBase.EVENT_ID)));  
			iw.setDataType(cursor.getString(cursor.getColumnIndex(MediaTableSchemaBase.DATA_TYPE))); 
			iw.setData(cursor.getString(cursor.getColumnIndex(MediaTableSchemaBase.DATA)));  
			iw.setCreatedDate(cursor.getLong(cursor.getColumnIndex(MediaTableSchemaBase.CREATED_DATE)));  
			iw.setModifiedDate(cursor.getLong(cursor.getColumnIndex(MediaTableSchemaBase.MODIFIED_DATE)));  
			iw.set_ReceivedDate(cursor.getLong(cursor.getColumnIndex(MediaTableSchemaBase._RECEIVED_DATE))); 
			iw.set_Disposition(cursor.getInt(cursor.getColumnIndex(MediaTableSchemaBase._DISPOSITION))); 

			Gson gson = new Gson();

			try {
				eos.writeBytes(gson.toJson(iw));
				eos.writeByte(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// not a reference field name :event id eventId event_id\n 
			try {
				String fileName = iw.getData(); 
				File dataFile = new File(fileName);
				int dataSize = (int)dataFile.length();
				byte[] buffData = new byte[dataSize];
				FileInputStream fileStream = new FileInputStream(dataFile);
				int ret = 0;   
				for (int position = 0; (ret > -1 && dataSize > position); position += ret) {
					ret = fileStream.read(buffData, position, (int)(dataSize - position));
				}
				fileStream.close();

				eos.writeBytes("data"); 
				eos.writeByte(0);

				ByteBuffer dataSizeBuf = ByteBuffer.allocate(Integer.SIZE/Byte.SIZE);
				dataSizeBuf.order(ByteOrder.LITTLE_ENDIAN);
				dataSizeBuf.putInt(dataSize);

				// write the media back out
				eos.write(dataSizeBuf.array());
				eos.write(buffData);
				eos.write(dataSizeBuf.array());
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} 
			// not a reference field name :created date createdDate created_date\n 
			// not a reference field name :modified date modifiedDate modified_date\n 
			// MediaTableSchemaBase._DISPOSITION;

			//           try {
			// TODO write to content provider using openFile
			// if (!applCacheMediaDir.exists() ) applCacheMediaDir.mkdirs();

			// File outfile = new File(applCacheMediaDir, Integer.toHexString((int) System.currentTimeMillis())); 
			//              BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(outfile), 8192);
			//              bufferedOutput.write(baos.toByteArray());
			//              bufferedOutput.flush();
			//              bufferedOutput.close();


			//           } catch (FileNotFoundException e) {
			//              e.printStackTrace();
			//           } catch (IOException e) {
			//              e.printStackTrace();
			//           }
		}
		return paths;
	} 
	//@Override 
	public ArrayList<File> eventSerialize(Cursor cursor) {
		logger.debug( "::eventSerialize");
		ArrayList<File> paths = new ArrayList<File>();      
		if (1 > cursor.getCount()) return paths;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream eos = new DataOutputStream(baos);

		for (boolean more = cursor.moveToFirst(); more; more = cursor.moveToNext()) {
			EventWrapper iw = new EventWrapper();
			iw.setUuid(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.UUID)));  
			iw.setMediaCount(cursor.getInt(cursor.getColumnIndex(EventTableSchemaBase.MEDIA_COUNT)));  
			iw.setOriginator(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.ORIGINATOR)));  
			iw.setDisplayName(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.DISPLAY_NAME)));  
			iw.setCategoryId(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.CATEGORY_ID)));  
			iw.setTitle(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.TITLE)));  
			iw.setDescription(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.DESCRIPTION)));  
			iw.setLongitude(cursor.getDouble(cursor.getColumnIndex(EventTableSchemaBase.LONGITUDE)));  
			iw.setLatitude(cursor.getDouble(cursor.getColumnIndex(EventTableSchemaBase.LATITUDE)));  
			iw.setCreatedDate(cursor.getLong(cursor.getColumnIndex(EventTableSchemaBase.CREATED_DATE)));  
			iw.setModifiedDate(cursor.getLong(cursor.getColumnIndex(EventTableSchemaBase.MODIFIED_DATE)));  
			iw.setCid(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.CID)));  
			iw.setCategory(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.CATEGORY)));  
			iw.setUnit(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.UNIT)));  
			iw.setSize(cursor.getLong(cursor.getColumnIndex(EventTableSchemaBase.SIZE)));  
			iw.setDestGroupType(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.DEST_GROUP_TYPE)));  
			iw.setDestGroupName(cursor.getString(cursor.getColumnIndex(EventTableSchemaBase.DEST_GROUP_NAME)));  
			iw.setStatus(cursor.getInt(cursor.getColumnIndex(EventTableSchemaBase.STATUS)));  
			iw.set_ReceivedDate(cursor.getLong(cursor.getColumnIndex(EventTableSchemaBase._RECEIVED_DATE))); 
			iw.set_Disposition(cursor.getInt(cursor.getColumnIndex(EventTableSchemaBase._DISPOSITION))); 

			Gson gson = new Gson();

			try {
				eos.writeBytes(gson.toJson(iw));
				eos.writeByte(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// not a reference field name :uuid uuid uuid\n 
			// not a reference field name :media count mediaCount media_count\n 
			// not a reference field name :originator originator originator\n 
			// not a reference field name :display name displayName display_name\n 
			// not a reference field name :category id categoryId category_id\n 
			// not a reference field name :title title title\n 
			// not a reference field name :description description description\n 
			// not a reference field name :longitude longitude longitude\n 
			// not a reference field name :latitude latitude latitude\n 
			// not a reference field name :created date createdDate created_date\n 
			// not a reference field name :modified date modifiedDate modified_date\n 
			// not a reference field name :cid cid cid\n 
			// not a reference field name :category category category\n 
			// not a reference field name :unit unit unit\n 
			// not a reference field name :size size size\n 
			// not a reference field name :dest group type destGroupType dest_group_type\n 
			// not a reference field name :dest group name destGroupName dest_group_name\n 
			// not a reference field name :STATUS status status\n 
			// EventTableSchemaBase._DISPOSITION;

			//           try {
			// TODO write to content provider using openFile
			// if (!applCacheEventDir.exists() ) applCacheEventDir.mkdirs();

			// File outfile = new File(applCacheEventDir, Integer.toHexString((int) System.currentTimeMillis())); 
			//              BufferedOutputStream bufferedOutput = 
			//            		  new BufferedOutputStream(new FileOutputStream(outfile), 8192);
			//              bufferedOutput.write(baos.toByteArray());
			//              bufferedOutput.flush();
			//              bufferedOutput.close();

			// paths.add(outfile);
			//           } catch (FileNotFoundException e) {
			//              e.printStackTrace();
			//           } catch (IOException e) {
			//              e.printStackTrace();
			//           }
		}
		return paths;
	} 
	//@Override 
	public ArrayList<File> categorySerialize(Cursor cursor) {
		logger.debug( "::categorySerialize");
		ArrayList<File> paths = new ArrayList<File>();      
		if (1 > cursor.getCount()) return paths;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream eos = new DataOutputStream(baos);

		for (boolean more = cursor.moveToFirst(); more; more = cursor.moveToNext()) {
			CategoryWrapper iw = new CategoryWrapper();
			iw.setMainCategory(cursor.getString(cursor.getColumnIndex(CategoryTableSchemaBase.MAIN_CATEGORY)));  
			iw.setSubCategory(cursor.getString(cursor.getColumnIndex(CategoryTableSchemaBase.SUB_CATEGORY)));  
			iw.setTigrId(cursor.getString(cursor.getColumnIndex(CategoryTableSchemaBase.TIGR_ID)));  
			iw.setIconType(cursor.getString(cursor.getColumnIndex(CategoryTableSchemaBase.ICON_TYPE))); 
			iw.setIcon(cursor.getString(cursor.getColumnIndex(CategoryTableSchemaBase.ICON)));  
			iw.set_ReceivedDate(cursor.getLong(cursor.getColumnIndex(CategoryTableSchemaBase._RECEIVED_DATE))); 
			iw.set_Disposition(cursor.getInt(cursor.getColumnIndex(CategoryTableSchemaBase._DISPOSITION))); 

			Gson gson = new Gson();

			try {
				eos.writeBytes(gson.toJson(iw));
				eos.writeByte(0);
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// not a reference field name :main category mainCategory main_category\n 
			// not a reference field name :sub category subCategory sub_category\n 
			// not a reference field name :tigr id tigrId tigr_id\n 
			try {
				String fileName = iw.getIcon(); 
				File dataFile = new File(fileName);
				int dataSize = (int)dataFile.length();
				byte[] buffData = new byte[dataSize];
				FileInputStream fileStream = new FileInputStream(dataFile);
				int ret = 0;   
				for (int position = 0; (ret > -1 && dataSize > position); position += ret) {
					ret = fileStream.read(buffData, position, (int)(dataSize - position));
				}
				fileStream.close();

				eos.writeBytes("icon"); 
				eos.writeByte(0);

				ByteBuffer dataSizeBuf = ByteBuffer.allocate(Integer.SIZE/Byte.SIZE);
				dataSizeBuf.order(ByteOrder.LITTLE_ENDIAN);
				dataSizeBuf.putInt(dataSize);

				// write the category back out
				eos.write(dataSizeBuf.array());
				eos.write(buffData);
				eos.write(dataSizeBuf.array());
			} catch(FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e) {
				e.printStackTrace();
			} 
			// CategoryTableSchemaBase._DISPOSITION;

			//           try {
			//              if (!applCacheCategoryDir.exists() ) applCacheCategoryDir.mkdirs();
			//              
			//              File outfile = new File(applCacheCategoryDir, Integer.toHexString((int) System.currentTimeMillis())); 
			//              BufferedOutputStream bufferedOutput = new BufferedOutputStream(new FileOutputStream(outfile), 8192);
			//              bufferedOutput.write(baos.toByteArray());
			//              bufferedOutput.flush();
			//              bufferedOutput.close();
			//           
			//              paths.add(outfile);
			//           } catch (FileNotFoundException e) {
			//              e.printStackTrace();
			//           } catch (IOException e) {
			//              e.printStackTrace();
			//           }
		}
		return paths;
	} 

	class mediaDeserializer implements IMyWriter {

		public long meta(StringBuilder sb) {
			String json = sb.toString();
			Gson gson = new Gson();
			MediaWrapper wrap = null;
			try {
				wrap = gson.fromJson(json, MediaWrapper.class);
			} catch (JsonParseException ex) {
				ex.getMessage();
				ex.printStackTrace();
				return -1;
			} catch (java.lang.RuntimeException ex) {
				ex.getMessage();
				ex.printStackTrace();
				return -1;
			}
			if (wrap == null) return -1;

			//SQLiteDatabase db = openHelper.getReadableDatabase();

			ContentValues cv = mediaComposeValues(wrap);
			// Put the current system time into the received column for relative time pulls.
			cv.put(MediaTableSchemaBase._RECEIVED_DATE, System.currentTimeMillis());
			// String whereClause = mediaSelectKeyClause(wrap);

			//         if (whereClause != null) {
			//            // Switch on the path in the uri for what we want to query.
			//            Cursor updateCursor = db.query(Tables.MEDIA_TBL, mediaProjectionKey, whereClause, null, null, null, null);
			//            long rowId = -1;
			//            for (boolean more = updateCursor.moveToFirst(); more;)
			//            {
			//                rowId = updateCursor.getLong(updateCursor.getColumnIndex(MediaTableSchemaBase._ID));  
			// 
			//                db.update(Tables.MEDIA_TBL, cv, 
			//                       "\""+MediaTableSchemaBase._ID+"\" = '"+ Long.toString(rowId)+"'",
			//                        null); 
			//                break;
			//            }
			//            updateCursor.close();
			//            if (rowId > 0) {
			//                getContext().getContentResolver().notifyChange(MediaTableSchemaBase.CONTENT_URI, null); 
			//                return rowId;
			//            }
			//         }
			//long rowId = db.insert(Tables.MEDIA_TBL, 
			//         MediaTableSchemaBase.EVENT_ID,
			//         cv);
			Uri rowUri = getContext().getContentResolver().insert(MediaTableSchemaBase.CONTENT_URI, cv);
			long rowId = Long.valueOf(rowUri.getLastPathSegment()).longValue();

			getContext().getContentResolver().notifyChange(MediaTableSchemaBase.CONTENT_URI, null); 
			return rowId;
		}

		@Override
		public long payload(long rowId, String label, byte[] buf) {
			ContentResolver cr = getContext().getContentResolver();
			Uri rowUri = ContentUris.withAppendedId(MediaTableSchemaBase.CONTENT_URI, rowId);
			Cursor cursor = cr.query(rowUri, null, null, null, null);
			cursor.moveToFirst();
			String filename = cursor.getString(cursor.getColumnIndex(label));  
			cursor.close();
			File dataFile = new File(filename);
			File dataDir = dataFile.getParentFile();
			if (!dataDir.exists()) {
				dataDir.mkdirs();
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(dataFile);
			} catch (FileNotFoundException e) {
				return -1;
			}
			try {
				fos.write(buf);
				fos.close();
			} catch (IOException e) {
				return -1;
			}
			return 0;
		}
	}

	public long mediaDeserialize(File file) {
		return this.deserializer(file, new mediaDeserializer());
	} 

	class eventDeserializer implements IMyWriter {

		public long meta(StringBuilder sb) {
			String json = sb.toString();
			Gson gson = new Gson();
			EventWrapper wrap = null;
			try {
				wrap = gson.fromJson(json, EventWrapper.class);
			} catch (JsonParseException ex) {
				ex.getMessage();
				ex.printStackTrace();
				return -1;
			} catch (java.lang.RuntimeException ex) {
				ex.getMessage();
				ex.printStackTrace();
				return -1;
			}
			if (wrap == null) return -1;

			//SQLiteDatabase db = openHelper.getReadableDatabase();

			ContentValues cv = eventComposeValues(wrap);
			// Put the current system time into the received column for relative time pulls.
			cv.put(EventTableSchemaBase._RECEIVED_DATE, System.currentTimeMillis());
			// String whereClause = eventSelectKeyClause(wrap);

			//         if (whereClause != null) {
			//            // Switch on the path in the uri for what we want to query.
			//            Cursor updateCursor = db.query(Tables.EVENT_TBL, eventProjectionKey, whereClause, null, null, null, null);
			//            long rowId = -1;
			//            for (boolean more = updateCursor.moveToFirst(); more;)
			//            {
			//                rowId = updateCursor.getLong(updateCursor.getColumnIndex(EventTableSchemaBase._ID));  
			// 
			//                db.update(Tables.EVENT_TBL, cv, 
			//                       "\""+EventTableSchemaBase._ID+"\" = '"+ Long.toString(rowId)+"'",
			//                        null); 
			//                break;
			//            }
			//            updateCursor.close();
			//            if (rowId > 0) {
			//                getContext().getContentResolver().notifyChange(EventTableSchemaBase.CONTENT_URI, null); 
			//                return rowId;
			//            }
			//         }
			//long rowId = db.insert(Tables.EVENT_TBL, 
			//         EventTableSchemaBase.UUID,
			//         cv);
			Uri rowUri = getContext().getContentResolver().insert(EventTableSchemaBase.CONTENT_URI, cv);
			long rowId = Long.valueOf(rowUri.getLastPathSegment()).longValue();

			getContext().getContentResolver().notifyChange(EventTableSchemaBase.CONTENT_URI, null); 
			return rowId;
		}

		@Override
		public long payload(long rowId, String label, byte[] buf) {
			ContentResolver cr = getContext().getContentResolver();
			Uri rowUri = ContentUris.withAppendedId(EventTableSchemaBase.CONTENT_URI, rowId);
			Cursor cursor = cr.query(rowUri, null, null, null, null);
			cursor.moveToFirst();
			String filename = cursor.getString(cursor.getColumnIndex(label));  
			cursor.close();
			File dataFile = new File(filename);
			File dataDir = dataFile.getParentFile();
			if (!dataDir.exists()) {
				dataDir.mkdirs();
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(dataFile);
			} catch (FileNotFoundException e) {
				return -1;
			}
			try {
				fos.write(buf);
				fos.close();
			} catch (IOException e) {
				return -1;
			}
			return 0;
		}
	}

	public long eventDeserialize(File file) {
		return this.deserializer(file, new eventDeserializer());
	} 

	class categoryDeserializer implements IMyWriter {

		public long meta(StringBuilder sb) {
			String json = sb.toString();
			Gson gson = new Gson();
			CategoryWrapper wrap = null;
			try {
				wrap = gson.fromJson(json, CategoryWrapper.class);
			} catch (JsonParseException ex) {
				ex.getMessage();
				ex.printStackTrace();
				return -1;
			} catch (java.lang.RuntimeException ex) {
				ex.getMessage();
				ex.printStackTrace();
				return -1;
			}
			if (wrap == null) return -1;

			//SQLiteDatabase db = openHelper.getReadableDatabase();

			ContentValues cv = categoryComposeValues(wrap);
			// Put the current system time into the received column for relative time pulls.
			cv.put(CategoryTableSchemaBase._RECEIVED_DATE, System.currentTimeMillis());
			// String whereClause = categorySelectKeyClause(wrap);

			//         if (whereClause != null) {
			//            // Switch on the path in the uri for what we want to query.
			//            Cursor updateCursor = db.query(Tables.CATEGORY_TBL, categoryProjectionKey, whereClause, null, null, null, null);
			//            long rowId = -1;
			//            for (boolean more = updateCursor.moveToFirst(); more;)
			//            {
			//                rowId = updateCursor.getLong(updateCursor.getColumnIndex(CategoryTableSchemaBase._ID));  
			// 
			//                db.update(Tables.CATEGORY_TBL, cv, 
			//                       "\""+CategoryTableSchemaBase._ID+"\" = '"+ Long.toString(rowId)+"'",
			//                        null); 
			//                break;
			//            }
			//            updateCursor.close();
			//            if (rowId > 0) {
			//                getContext().getContentResolver().notifyChange(CategoryTableSchemaBase.CONTENT_URI, null); 
			//                return rowId;
			//            }
			//         }
			//long rowId = db.insert(Tables.CATEGORY_TBL, 
			//         CategoryTableSchemaBase.MAIN_CATEGORY,
			//         cv);
			Uri rowUri = getContext().getContentResolver().insert(CategoryTableSchemaBase.CONTENT_URI, cv);
			long rowId = Long.valueOf(rowUri.getLastPathSegment()).longValue();

			getContext().getContentResolver().notifyChange(CategoryTableSchemaBase.CONTENT_URI, null); 
			return rowId;
		}

		@Override
		public long payload(long rowId, String label, byte[] buf) {
			ContentResolver cr = getContext().getContentResolver();
			Uri rowUri = ContentUris.withAppendedId(CategoryTableSchemaBase.CONTENT_URI, rowId);
			Cursor cursor = cr.query(rowUri, null, null, null, null);
			cursor.moveToFirst();
			String filename = cursor.getString(cursor.getColumnIndex(label));  
			cursor.close();
			File dataFile = new File(filename);
			File dataDir = dataFile.getParentFile();
			if (!dataDir.exists()) {
				dataDir.mkdirs();
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(dataFile);
			} catch (FileNotFoundException e) {
				return -1;
			}
			try {
				fos.write(buf);
				fos.close();
			} catch (IOException e) {
				return -1;
			}
			return 0;
		}
	}

	public long categoryDeserialize(File file) {
		return this.deserializer(file, new categoryDeserializer());
	} 



}
