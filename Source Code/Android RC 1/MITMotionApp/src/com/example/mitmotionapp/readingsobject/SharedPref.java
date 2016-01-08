package com.example.mitmotionapp.readingsobject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

@SuppressLint("CommitPrefEdits")
public class SharedPref {
	// shared preferences object
	SharedPreferences pref;

	// Editor for shared Preferences
	Editor editor;

	// context
	Context context;

	// Shared Preference mode
	int private_mode = 0;

	// Shared preferences file name
	static final String PREF_NAME = "READING_FREQUENCIES";

	// GPS frequency
	public static final String GPS_FREQUENCY = "Gps_Frequency";

	// accelerometer frequency
	public static final String ACCEL_FREQUENCY = "Accelerometer_Frequency";

	// compass frequency
	public static final String COMPASS_FREQUENCY = "Compass_Frequency";

	// gyroscope frequency
	public static final String GYRO_FREQUENCY = "Gyro_Frequency";

	// gyroscope frequency
	public static final String GPS_SWITCH = "Gps_Switch";
	// gyroscope frequency
	public static final String ACCEL_SWITCH = "Accel_Switch";
	// gyroscope frequency
	public static final String GYRO_SWITCH = "Gyro_Switch";
	// gyroscope frequency
	public static final String COMPASS_SWITCH = "Compass_Switch";

	public static final String VIN_PREF = "Vin_values";

	// FTP name, user, pass, port
	public static final String FTP_NAME = "Ftp_Name";
	public static final String FTP_USER = "Ftp_UserName";
	public static final String FTP_PASS = "Ftp_Pass";
	public static final String FTP_PORT = "Ftp_Port";

	// TCP
	public static final String TCP_ADDRESS = "TCP_ADDRESS";
	public static final String TCP_PORT = "TCP_PORT";
	public static final String TCP_USERID = "TCP_USERID";

	// email id
	public static final String EMAIL_ADDRESS = "Email_address";

	// email id
	public static final String CHECKIN_TYPE = "Checkintype";
	
	// Constructor
	public SharedPref(Context cntx) {
		this.context = cntx;
		pref = context.getSharedPreferences(PREF_NAME, private_mode);
		editor = pref.edit();
	}

	// save Value
	public void save_prefValues(String key, String Value) {
		editor.putString(key, Value);
		editor.commit();
	}

	// get preferences value
	public String get_prefValue(String key) {
		return pref.getString(key, null);
	}

}
