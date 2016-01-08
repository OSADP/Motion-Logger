package com.example.mitmotionapp.databases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mitmotionapp.accelerometer.AccelerometerReadings;
import com.example.mitmotionapp.compass.CompassReadings;
import com.example.mitmotionapp.gps.ContextObject;
import com.example.mitmotionapp.gps.GpsReadings;
import com.example.mitmotionapp.gps.GroundObject;
import com.example.mitmotionapp.gyroscope.GyroReadings;
import com.example.mitmotionapp.readingsobject.MainObject;
import com.example.mitmotionapp.rest.Rest;

public class DBAdapter {
	
		static final String TAG="DBAdapter";
		// RECORDS TABLE fields Information........
		static final String KEY_RECORD_ID			="record_id";
		static final String KEY_RECORD_NAME			="record_name";
		static final String KEY_RECORD_TIME			="record_time";
		static final String KEY_RECORD_DURATION		="record_duration";
		static final String KEY_RECORD_GPS_STATUS	="isGpsOn";
		static final String KEY_RECORD_ACCEL_STATUS	="isAccelOn";
		static final String KEY_RECORD_GYRO_STATUS	="isGyroOn";
		static final String KEY_RECORD_COMPASS_STATUS="isComOn";
		static final String KEY_RECORD_GPS_Quality="gpsQuality";
		
		// GPS TABLE fields Information........
		static final String KEY_GPS_ID				="gps_id";
		static final String KEY_GPS_TIME			="timeStamp";
		static final String KEY_GPS_LAT				="lat";
		static final String KEY_GPS_LONG			="long";
		static final String KEY_GPS_HEIGHT			="height";
		
		// ACCEL TABLE fields Information........
		static final String KEY_ACCEL_ID			="accel_id";
		static final String KEY_ACCEL_X				="x";
		static final String KEY_ACCEL_Y				="y";
		static final String KEY_ACCEL_Z				="z";
		static final String KEY_ACCEL_TIME			="timeStamp";
		
		// GYRO TABLE fields Information........
		static final String KEY_GYRO_ID="gyro_id";
		static final String KEY_GYRO_X="x";
		static final String KEY_GYRO_Y="y";
		static final String KEY_GYRO_Z="z";
		static final String KEY_GYRO_TIME="timeStamp";
		
		// COMPASS TABLE fields Information........
		static final String KEY_COMPASS_ID="compass_id";
		static final String KEY_COMPASS_MHEADING="magHeading";
		static final String KEY_COMPASS_THEADING="trueHeading";
		static final String KEY_COMPASS_TIME="timeStamp";	
		// CONTEXT TABLE fields Information........
		static final String KEY_CONTEXT_ID="context_id";
		static final String KEY_CONTEXT_STATE="contextState";
		static final String KEY_CONTEXT_TIME="timeStamp";	
		// GROUND TABLE fields Information........
		static final String KEY_GROUND_ID="ground_id";
		static final String KEY_GROUND_STATE="groundState";
		static final String KEY_GROUND_TIME="timeStamp";	
		// REST TABLE fields Information........
		static final String KEY_REST_ID="rest_id";
		static final String KEY_REST_STATE="restState";
		static final String KEY_REST_CHECKIN="checkin";
		static final String KEY_REST_TIME="timeStamp";	
		
		//DataBase Information.......
		static final String DATABASE_NAME="MotionApp.sqlite";
		static final String DATABASE_PATH="/data/data/com.jsiegel.mitmotionapp/databases/";
		static final String DATABASE_TABLE_RECORD="Record";
		static final String DATABASE_TABLE_GPS="Gps";
		static final String DATABASE_TABLE_ACCEL="Accel";
		static final String DATABASE_TABLE_GYRO="Gyro";
		static final String DATABASE_TABLE_COMPASS="Compass";
		static final String DATABASE_TABLE_REST="Rest";
		static final String DATABASE_TABLE_GROUND="Ground";
		static final String DATABASE_TABLE_CONTEXT="ContextValue";
		
		static final int DATABASE_VERSION=1;
		
//Other Variables........
	final Context context;
	DataBaseHelper DBHelper;
	static SQLiteDatabase db;

//DBAdapter Constructor....	
	public DBAdapter(Context ctx){
		this.context = ctx;
		DBHelper = new DataBaseHelper(context);
	}

// DataBase Helper Class Extends From SQLite OpenHelper.....
	public static class DataBaseHelper extends SQLiteOpenHelper {
		
	//Some Variables....
		private Context ctx2;

//HelperClass Constructor....
		DataBaseHelper(Context context) {
			
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.ctx2 = context;
			boolean dbexist = checkdatabase();
			try{
			 if (dbexist) {
		            System.out.println("Database exists");
		            openDB(); 
		        } else {
		            System.out.println("Database doesn't exist");
		            createDB();
		            openDB(); 
		        }
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
		
//Override SQLite OpenHelper Class Abstract Methods.....
		@Override
		public void onCreate(SQLiteDatabase db) {
			 Log.v(TAG,"On create Called:"+db.getPath());
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
		
// my Own Functions...
		public void createDB()throws IOException {
			this.getReadableDatabase();
            copyDB();
		}
		private boolean checkdatabase(){
			 boolean checkdb = false;
		        try {
		            String myPath = DATABASE_PATH + DATABASE_NAME;
		            File dbfile = new File(myPath);
		            checkdb = dbfile.exists();
		        } catch(SQLiteException e) {
		            System.out.println("Database doesn't exist");
		        }
		        return checkdb;
		}
		public void copyDB()throws IOException {
	        //Open your local db as the input stream
	        InputStream myinput = ctx2.getAssets().open(DATABASE_NAME);
	        // Path to the just created empty db
	        String outfilename = DATABASE_PATH + DATABASE_NAME;
	        //Open the empty db as the output stream
	        OutputStream myoutput = new FileOutputStream(outfilename);
	        // transfer byte to input file to output file
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = myinput.read(buffer))>0) {
	            myoutput.write(buffer,0,length);
	        }
	        //Close the streams
	        myoutput.flush();
	        myoutput.close();
	        myinput.close();
		
	    }
		public void openDB() throws SQLException {
	        //Open the database
	        String mypath = DATABASE_PATH + DATABASE_NAME;
	        db = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
	    }
		
		}
	//Closes the database.....
		public void close(){
			DBHelper.close();
		}
	// ---Retrieves Values----
		public Cursor getAllRecords(){
			return db.query(DATABASE_TABLE_RECORD, new String[]{KEY_RECORD_ID, KEY_RECORD_NAME, KEY_RECORD_TIME}, null, null, null, null,null);
		}
		
		//---retrieves a particular Row from Record Table---
	    public Cursor getRecordData(long rowId) throws SQLException 
	    {
	        Cursor mCursor =
	                db.query(true, DATABASE_TABLE_RECORD, new String[] {KEY_RECORD_ID,
	                KEY_RECORD_NAME, KEY_RECORD_TIME, KEY_RECORD_DURATION, KEY_RECORD_GPS_STATUS,KEY_RECORD_ACCEL_STATUS,KEY_RECORD_GYRO_STATUS,KEY_RECORD_COMPASS_STATUS,KEY_RECORD_GPS_Quality}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        if (mCursor != null) {
	          mCursor.moveToFirst();
	       }
	        return mCursor;
	    }
	  //---retrieves a particular Row from Record Table---
	    public Cursor getGpsData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_GPS, new String[] {KEY_GPS_LAT, KEY_GPS_LONG, KEY_GPS_HEIGHT, KEY_GPS_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	       
	        return mCursor;
	    }
	  //---retrieves a particular Row from Record Table---
	    public Cursor getAccelData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_ACCEL, new String[] {KEY_ACCEL_X, KEY_ACCEL_Y, KEY_ACCEL_Z, KEY_ACCEL_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        return mCursor;
	    }
	    //---retrieves a particular Row from Record Table---
	    public Cursor getContextData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_CONTEXT, new String[] {KEY_CONTEXT_STATE, KEY_CONTEXT_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        return mCursor;
	    }
	    //---retrieves a particular Row from Record Table---
	    public Cursor getGroundData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_CONTEXT, new String[] {KEY_GROUND_STATE, KEY_GROUND_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        return mCursor;
	    }
	  //---retrieves a particular Row from Record Table---
	    public Cursor getGyroData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_GYRO, new String[] {KEY_GYRO_X, KEY_GYRO_Y, KEY_GYRO_Z, KEY_GYRO_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        return mCursor;
	    }
	    
	    //---retrieves a particular Row from Record Table---
	    public Cursor getCompassData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_COMPASS, new String[] {KEY_COMPASS_MHEADING, KEY_COMPASS_THEADING, KEY_COMPASS_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        return mCursor;
	    }
	    //---retrieves a particular Row from Record Table---
	    public Cursor getRestData(long rowId) throws SQLException 
	    {
	        Cursor mCursor = db.query(true, DATABASE_TABLE_REST, new String[] {KEY_REST_STATE, KEY_REST_CHECKIN, KEY_REST_TIME}, KEY_RECORD_ID + "=" + rowId, null,
	                null, null, null, null);
	        return mCursor;
	    }

	  //---retrieves a particular Row from Record Table---
	    public void deleteRecordData(long recordid) throws SQLException 
	    {
	    	db.delete(DATABASE_TABLE_RECORD,"record_id=?",new String[] { String.valueOf(recordid) });
	    	db.delete(DATABASE_TABLE_GPS,"record_id=?",new String[] { String.valueOf(recordid)});
	    	db.delete(DATABASE_TABLE_ACCEL,"record_id=?",new String[] { String.valueOf(recordid)});
	    	db.delete(DATABASE_TABLE_GYRO,"record_id=?",new String[] { String.valueOf(recordid)});	    	
	    	db.delete(DATABASE_TABLE_COMPASS,"record_id=?",new String[] { String.valueOf(recordid)});
	    	db.delete(DATABASE_TABLE_REST,"record_id=?",new String[] { String.valueOf(recordid)});	    	
	    	db.delete(DATABASE_TABLE_GROUND,"record_id=?",new String[] { String.valueOf(recordid)});
	    	db.delete(DATABASE_TABLE_CONTEXT,"record_id=?",new String[] { String.valueOf(recordid)});	    	
	    	db.delete(DATABASE_TABLE_CONTEXT,"record_id=?",new String[] { String.valueOf(recordid)});	    	
	    }

		
		public void dbBeginTransaction() {
			db.beginTransaction();
		}
		
		public void dbEndTransaction() {
			db.setTransactionSuccessful(); 
			db.endTransaction();
		}
		
		//---insert into MainRecord database Table---
	    public long insertInRecordTable(MainObject mainObj) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_NAME, mainObj.record_name);
	        initialValues.put(KEY_RECORD_TIME, mainObj.record_time);
	        initialValues.put(KEY_RECORD_DURATION, mainObj.record_duration);
	        initialValues.put(KEY_RECORD_GPS_STATUS, mainObj.gpsStatus());
	        initialValues.put(KEY_RECORD_ACCEL_STATUS, mainObj.accelStatus());
	        initialValues.put(KEY_RECORD_GYRO_STATUS, mainObj.gyroStatus());
	        initialValues.put(KEY_RECORD_COMPASS_STATUS, mainObj.compStatus());
	        initialValues.put(KEY_RECORD_GPS_Quality, mainObj.gpsAccuraccy);
	        try { return db.insert(DATABASE_TABLE_RECORD, null, initialValues); } catch (Exception e) { return -1; }
	    }
	    
	  //---insert into GPS database Table---
	    public long insertInGpsTable(GpsReadings gpsObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_GPS_LAT, gpsObj.latitude);
	        initialValues.put(KEY_GPS_LONG, gpsObj.longitude);
	        initialValues.put(KEY_GPS_HEIGHT, gpsObj.altitude);
	        initialValues.put(KEY_GPS_TIME, gpsObj.timeStamp);
	        try { return db.insert(DATABASE_TABLE_GPS, null, initialValues); } catch (Exception e) { return -1; }
	    }
	    
	  //---insert into Accel database Table---
	    public long insertInAccelTable(AccelerometerReadings accelObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_ACCEL_X, accelObj.accelXReading);
	        initialValues.put(KEY_ACCEL_Y, accelObj.accelYReading);
	        initialValues.put(KEY_ACCEL_Z, accelObj.accelZReading);
	        initialValues.put(KEY_ACCEL_TIME, accelObj.timeStamp);
	        try { return db.insert(DATABASE_TABLE_ACCEL, null, initialValues); } catch (Exception e) { return -1; }
	    }
	  //---insert into Compass database Table---
	    public long insertInCompassTable(CompassReadings comObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_COMPASS_MHEADING, comObj.mHeading);
	        initialValues.put(KEY_COMPASS_THEADING, comObj.tHeading);
	        initialValues.put(KEY_COMPASS_TIME, comObj.timeStamp);
	        try { return db.insert(DATABASE_TABLE_COMPASS, null, initialValues); } catch (Exception e) { return -1; }
	    }
	  //---insert into Context database Table---
	    public long insertInContextTable(ContextObject conObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_CONTEXT_STATE, conObj.contextValue);
	        initialValues.put(KEY_CONTEXT_TIME, conObj.timeStamp);
	        try { return db.insert(DATABASE_TABLE_CONTEXT, null, initialValues); } catch (Exception e) { return -1; }
	    }
		//---insert into Context database Table---
	    public long insertInGroundTable(GroundObject gndObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_GROUND_STATE, gndObj.groundValue);
	        initialValues.put(KEY_GROUND_TIME, gndObj.timeStamp);
	        try { return db.insert(DATABASE_TABLE_CONTEXT, null, initialValues); } catch (Exception e) { return -1; }
	    }
	    //---insert into Gyro database Table---
	    public long insertInGyroTable(GyroReadings gyroObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_GYRO_X, gyroObj.gyroXReading);
	        initialValues.put(KEY_GYRO_Y, gyroObj.gyroYReading);
	        initialValues.put(KEY_GYRO_Z, gyroObj.gyroZReading);
	        initialValues.put(KEY_GYRO_TIME, gyroObj.timeStamp);
	        try { return db.insert(DATABASE_TABLE_GYRO, null, initialValues); } catch (Exception e) { return -1; }
	    }
	    
	    public long insertInRestTable(Rest restObj, long recordId) 
	    {
	        ContentValues initialValues = new ContentValues();
	        initialValues.put(KEY_RECORD_ID, recordId);
	        initialValues.put(KEY_REST_STATE, restObj.restState);
	        initialValues.put(KEY_REST_CHECKIN, restObj.checkin);
	        initialValues.put(KEY_REST_TIME, restObj.timeStamp);   
	        try { return db.insert(DATABASE_TABLE_REST, null, initialValues); } catch (Exception e) { return -1; }
	    }
	    
	    //---updates a record Name---
	    public boolean updateRecordName(long rowId, String name) 
	    {
	        ContentValues args = new ContentValues();
	        args.put(KEY_RECORD_NAME, name);
	        return db.update(DATABASE_TABLE_RECORD, args, KEY_RECORD_ID + "=" + rowId, null) > 0;
	    }
		
	}


