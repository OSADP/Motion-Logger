package com.example.mitmotionapp.records;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.jsiegel.mitmotionapp.R;
import com.example.mitmotionapp.accelerometer.AccelerometerReadings;
import com.example.mitmotionapp.compass.CompassReadings;
import com.example.mitmotionapp.databases.DBAdapter;
import com.example.mitmotionapp.gps.ContextObject;
import com.example.mitmotionapp.gps.GpsReadings;
import com.example.mitmotionapp.gyroscope.GyroReadings;
import com.example.mitmotionapp.readingsobject.MainObject;
import com.example.mitmotionapp.readingsobject.SharedPref;
import com.example.mitmotionapp.rest.AsyncResponse;
import com.example.mitmotionapp.rest.Rest;
import com.example.mitmotionapp.rest.RestQuery;
import com.example.utils.IPredicate;
import com.example.utils.Predicate;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;

public class StartRecords extends BaseFragment implements SensorEventListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener, AsyncResponse {

	// ActivityRecognition....
	public ActivityRecognitionClient arclient;
	public PendingIntent pIntent;
	public BroadcastReceiver receiver;

	public Handler message_handler;

	Button leftBtn, rightBtn, dialogSaveBtn, dialogCancelBtn, dialogDiscardBtn;

	TextView startTimeTV, recordDurationTV, longiTV, latiTV, altiTV, accelxTV,
			accelyTV, accelzTV, gpsQualityTV, trueHeadingTV, magneticHeadingTV,
			gyroXTV, gyroYTV, gyroZTV, contextValueTV;
	ImageView rest_icon;
	RelativeLayout rl_checkin;
	boolean m_bShowedCheckIn = false;
	boolean isRunning = false;
	Dialog dialog;
	Context m_Context;
	SimpleDateFormat timestampformat, timeFormat;
	Date m_dateStartTime;
	float mLastX, mLastY, mLastZ, deltaX, deltaY, deltaZ, gyroX, gyroY, gyroZ;

	Double longiValue, latiValue, altiValue;
	float accuracy;
	LocationManager mlocManager;
	LocationListener mlocListener;
	Location location;
	SensorManager mSensorManager;
	Sensor mAccelerometer, gyroScope, compass;
	String recordDuration;
	boolean m_bGpsOn, m_bAccelOn, m_bCompOn, m_bGryroOn;
	String m_ContextValue;
	String accelUpdateTime;
	SharedPref pref;
	DBAdapter db;

	float magnetic, comptrue;
	String tHeading, mHeading;
	float accelRate, gyroRate, compassRate;
	int aRate, gRate, cRate;

	long id;
	public ArrayList<GpsReadings> m_aryGps = new ArrayList<GpsReadings>();
	public ArrayList<AccelerometerReadings> m_aryAccel = new ArrayList<AccelerometerReadings>();
	public ArrayList<GyroReadings> m_aryGyro = new ArrayList<GyroReadings>();
	public ArrayList<CompassReadings> m_aryComp = new ArrayList<CompassReadings>();
	public ArrayList<ContextObject> m_aryContext = new ArrayList<ContextObject>();
	public ArrayList<Rest> m_aryRest = new ArrayList<Rest>();

	Date m_dateForTCP = null;
	int m_nTick;
	Handler m_handlerRecord;
	final static int RECORD_STATUS_STOP = 0;
	final static int RECORD_STATUS_REC = 1;
	final static int RECORD_STATUS_PAUSE = 2;
	int m_nRecordStatus = RECORD_STATUS_STOP;
	boolean m_bCompletedSendViaTcp = true;

	@SuppressWarnings("deprecation")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// initializing...
		View view = initView(inflater, container, savedInstanceState);

		rl_checkin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mActivity.pushFragments(AppConstants.TAB_A, new Checkin(),
						true, true);
			}
		});

		// GPS new....
		db = new DBAdapter(m_Context);
		pref = new SharedPref(m_Context);
		accelRate = Float.parseFloat(pref
				.get_prefValue(SharedPref.ACCEL_FREQUENCY) == null ? "0.0"
				: pref.get_prefValue(SharedPref.ACCEL_FREQUENCY));
		gyroRate = Float.parseFloat(pref
				.get_prefValue(SharedPref.GYRO_FREQUENCY) == null ? "0.0"
				: pref.get_prefValue(SharedPref.GYRO_FREQUENCY));
		compassRate = Float.parseFloat(pref
				.get_prefValue(SharedPref.COMPASS_FREQUENCY) == null ? "0.0"
				: pref.get_prefValue(SharedPref.COMPASS_FREQUENCY));

		aRate = (int) accelRate;
		gRate = (int) gyroRate;
		cRate = (int) compassRate;
 
		
		// Convert to uS
		if (aRate != 0) { aRate = (int)(1000000 / aRate); }
		if (gRate != 0) { gRate = (int)(1000000 / gRate); }
		if (cRate != 0) { cRate = (int)(1000000 / cRate); }
		
		mlocManager = (LocationManager) getActivity().getSystemService(
				Context.LOCATION_SERVICE);
		mlocListener = new MyLocationListener();
		mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,
				1, mlocListener);
		location = mlocManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		timestampformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSS",
				Locale.US);
		timeFormat = new SimpleDateFormat("HH:mm:ss.SSSSS", Locale.US);

		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		gyroScope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		compass = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		mSensorManager.registerListener(this, mAccelerometer, aRate);
		mSensorManager.registerListener(this, gyroScope, gRate);
		mSensorManager.registerListener(this, compass, cRate);

		m_bGpsOn = pref.get_prefValue(SharedPref.GPS_SWITCH) == null ? true
				: (pref.get_prefValue(SharedPref.GPS_SWITCH).equals("1"));
		m_bAccelOn = pref.get_prefValue(SharedPref.ACCEL_SWITCH) == null ? true
				: (pref.get_prefValue(SharedPref.ACCEL_SWITCH).equals("1"));
		m_bGryroOn = pref.get_prefValue(SharedPref.GYRO_SWITCH) == null ? true
				: (pref.get_prefValue(SharedPref.GYRO_SWITCH).equals("1"));
		m_bCompOn = pref.get_prefValue(SharedPref.COMPASS_SWITCH) == null ? true
				: (pref.get_prefValue(SharedPref.COMPASS_SWITCH).equals("1"));

		accelUpdateTime = pref.get_prefValue(SharedPref.ACCEL_FREQUENCY) == null ? "0"
				: pref.get_prefValue(SharedPref.ACCEL_FREQUENCY);

		int resp = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(m_Context);
		if (resp == ConnectionResult.SUCCESS) {
			arclient = new ActivityRecognitionClient(m_Context, this, this);
			arclient.connect();
		} else {
			Toast.makeText(m_Context, "Please install Google Play Service.",
					Toast.LENGTH_SHORT).show();
		}

		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context m_Context, Intent intent) {
				if (m_nRecordStatus == RECORD_STATUS_REC) {
					m_ContextValue = "Activity : "
							+ intent.getExtras().getString("Activity");
					contextFind();
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction("com.example.mitmotionapp.records.ACTIVITY_RECOGNITION_DATA");
		m_Context.registerReceiver(receiver, filter);

		// Click Listener....

		leftBtn.setOnClickListener(leftListener);
		rightBtn.setOnClickListener(rightListener);

		m_handlerRecord = new Handler();

		return view;
	}

	private View initView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.records, container, false);
		m_Context = getActivity();
		leftBtn = (Button) view.findViewById(R.id.header_leftBtn);
		rightBtn = (Button) view.findViewById(R.id.header_rightBtn);
		longiTV = (TextView) view.findViewById(R.id.longiTV);
		latiTV = (TextView) view.findViewById(R.id.latiTV);
		altiTV = (TextView) view.findViewById(R.id.altiTV);
		accelxTV = (TextView) view.findViewById(R.id.xtv);
		accelyTV = (TextView) view.findViewById(R.id.ytv);
		accelzTV = (TextView) view.findViewById(R.id.ztv);
		startTimeTV = (TextView) view.findViewById(R.id.starttimeTV);
		recordDurationTV = (TextView) view.findViewById(R.id.recorddurationTV);
		gpsQualityTV = (TextView) view.findViewById(R.id.gpsQulValuesTV);
		contextValueTV = (TextView) view.findViewById(R.id.contextvalueTV);
		trueHeadingTV = (TextView) view.findViewById(R.id.trueheading);
		magneticHeadingTV = (TextView) view.findViewById(R.id.magheading);
		rest_icon = (ImageView) view.findViewById(R.id.rest_indicator);
		rl_checkin = (RelativeLayout) view.findViewById(R.id.record_rl_checkin);
		gyroXTV = (TextView) view.findViewById(R.id.gyroXTV);
		gyroYTV = (TextView) view.findViewById(R.id.gyroYTV);
		gyroZTV = (TextView) view.findViewById(R.id.gyroZTV);

		return view;
	}

	public void processFinish(String result) {
		// System.out.println("Processing REST data");
		// System.out.println(result);

		Rest restObj = new Rest();

		restObj.timeStamp = timestampformat.format(new Date());

		long unix_lastseen = 0;
		long unix_requesttime = 0;

		try {
			JSONObject jsonResult = new JSONObject(result);
			SimpleDateFormat dfm1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (jsonResult.has("lastSeen")) {
				String lastSeen = jsonResult.getString("lastSeen");
				lastSeen = lastSeen.split("\\.")[0]; // Strip out the fractional
														// seconds

				try {
					unix_lastseen = dfm1.parse(lastSeen).getTime();
				}

				catch (ParseException e) {
					// System.out.println("Parsing error");
					e.printStackTrace();
				}
			}
			if (jsonResult.has("requestTime")) {
				String requestTime = jsonResult.getString("requestTime");
				requestTime = requestTime.split("\\.")[0]; // Strip out the
															// fractional
															// seconds

				try {
					unix_requesttime = dfm1.parse(requestTime).getTime();
				} catch (ParseException e) {
					// System.out.println("Parsing error");
					e.printStackTrace();
				}
			}
		} catch (JSONException e) {
			// System.out.println("JSON Exception");
			e.printStackTrace();
		} catch (Exception e) {
			// System.out.print("Error " + e.getClass());
		}
		// System.out.println(parsedData);

		// System.out.println(unix_lastseen);
		// System.out.println(unix_requesttime);

		if (unix_lastseen == 0 || unix_requesttime == 0) {
			// System.out.println("Error parsing one or more times.");
			// Error
			restObj.restState = "Error";
		} else if (unix_lastseen > (unix_requesttime - 300)) {
			// System.out.println("Seen within last 5 minutes");
			// Seen within five minutes
			restObj.restState = "Car active";
		} else {
			// System.out.println("Not seen in last 5 minutes");
			// Not seen within five minutes
			restObj.restState = "Car inactive";
		}

		if (restObj.restState == "Car active") {
			// CloudThink active
			rest_icon.setImageResource(R.drawable.greenbtn);
			// System.out.println("CloudThink car active");
		} else if (restObj.restState == "Car inactive") {
			// CloudThink inactive
			rest_icon.setImageResource(R.drawable.blackindc);
			// System.out.println("CloudThink car inactive");
		} else {
			// Error
			rest_icon.setImageResource(R.drawable.blackindc);
			// System.out.println("CloudThink car error");
		}

		restObj.checkin = pref.get_prefValue(SharedPref.CHECKIN_TYPE) == null ? "Empty"
				: pref.get_prefValue(SharedPref.CHECKIN_TYPE);

		m_aryRest.add(restObj);
//		showCheckInPage();
	}

	private void showCheckInPage() {
		if (m_bShowedCheckIn == false) {
			mActivity.pushFragments(AppConstants.TAB_A, new Checkin(), true,
					true);
			m_bShowedCheckIn = true;
		}
	}

	private OnClickListener leftListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			switch (m_nRecordStatus) {
			case RECORD_STATUS_STOP: // Stop to start
				m_nRecordStatus = RECORD_STATUS_REC;
				m_dateStartTime = new Date();
				m_handlerRecord.postDelayed(runnableRecord, 0);
				isRunning = true;
				break;
			case RECORD_STATUS_REC: // Recording to stop
				m_nRecordStatus = RECORD_STATUS_STOP;
				m_handlerRecord.removeCallbacks(runnableRecord);
				isRunning = false;
				break;
			case RECORD_STATUS_PAUSE: // TODO actual pausing
				m_nRecordStatus = RECORD_STATUS_STOP;
				m_handlerRecord.removeCallbacks(runnableRecord);
				isRunning = false;
				break;
			}
			showStartTime();
			showLRButtonTitle();

			if (m_nRecordStatus == RECORD_STATUS_STOP) {

				dialog = new Dialog(m_Context);
				dialog.setTitle("Create Log File");
				// dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				dialog.setContentView(R.layout.dialog_view);
				dialog.show();
				dialogSaveBtn = (Button) dialog.findViewById(R.id.saveBtn);
				dialogDiscardBtn = (Button) dialog.findViewById(R.id.discardBtn); // These are swapped! 
				dialogCancelBtn = (Button) dialog.findViewById(R.id.cancel);

				dialogSaveBtn.setOnClickListener(new Button.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialog.dismiss();

						MainObject mainobject = new MainObject();
						mainobject.record_time = timestampformat
								.format(m_dateStartTime);
						mainobject.record_duration = recordDuration;
						mainobject.record_name = "Unnamed Record";
						mainobject.gpsAccuraccy = Float.toString(accuracy);

						mainobject.isGpsOn = m_bGpsOn ? 1 : 0;
						mainobject.isAccelOn = m_bAccelOn ? 1 : 0;
						mainobject.isGyroOn = m_bGryroOn ? 1 : 0;
						mainobject.isCompassOn = m_bCompOn ? 1 : 0;

						id = db.insertInRecordTable(mainobject);

						db.dbBeginTransaction();
						try {
							for (GpsReadings array : m_aryGps) {
								db.insertInGpsTable(array, id);
							}
						} catch (Exception e) { }
						try {
							for (AccelerometerReadings array : m_aryAccel) {
								db.insertInAccelTable(array, id);
							}
						} catch (Exception e) { }
						try {
							for (CompassReadings array : m_aryComp) {
								db.insertInCompassTable(array, id);
							}
						} catch (Exception e) { }
						try {
							for (GyroReadings array : m_aryGyro) {
								db.insertInGyroTable(array, id);
							}
						} catch (Exception e) { }
						try {
							for (ContextObject array : m_aryContext) {
								// System.out.println("Dumping m_Context object to table.");
								db.insertInContextTable(array, id);
							}
						} catch (Exception e) { }
						try {
							for (Rest array : m_aryRest) {
								// System.out.println("Dumping REST object to table.");
								db.insertInRestTable(array, id);
							}
						} catch (Exception e) { }
						db.dbEndTransaction();
						db.close();

						recordDuration = "";
						m_nTick = 0;
						startTimeTV.setText("");
						recordDurationTV.setText(recordDuration);
						longiTV.setText("");
						latiTV.setText("");
						altiTV.setText("");
						accelxTV.setText("");
						accelyTV.setText("");
						accelzTV.setText("");
						gpsQualityTV.setText("");
						gyroXTV.setText("");
						gyroYTV.setText("");
						gyroZTV.setText("");
						contextValueTV.setText("");
						trueHeadingTV.setText("");
						magneticHeadingTV.setText("");

						m_aryAccel.clear();
						m_aryComp.clear();
						m_aryContext.clear();
						m_aryGps.clear();
						m_aryGyro.clear();
						m_aryRest.clear();
					}
				});

				dialogCancelBtn
						.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View v) {
								dialog.dismiss();
								recordDuration = "";
								startTimeTV.setText("");
								recordDurationTV.setText(recordDuration);
								longiTV.setText("");
								latiTV.setText("");
								altiTV.setText("");
								accelxTV.setText("");
								accelyTV.setText("");
								accelzTV.setText("");
								gpsQualityTV.setText("");
								gyroXTV.setText("");
								gyroYTV.setText("");
								gyroZTV.setText("");
								contextValueTV.setText("");
								trueHeadingTV.setText("");
								magneticHeadingTV.setText("");

//								m_aryAccel.clear();
//								m_aryComp.clear();
//								m_aryContext.clear();
//								m_aryGps.clear();
//								m_aryGyro.clear();
//								m_aryRest.clear();
							}
						});
				dialogDiscardBtn
						.setOnClickListener(new Button.OnClickListener() {
							@Override
							public void onClick(View v) {

								dialog.dismiss();
								recordDuration = "";
								m_nTick = 0;
								startTimeTV.setText("");
								recordDurationTV.setText(recordDuration);
								longiTV.setText("");
								latiTV.setText("");
								altiTV.setText("");
								accelxTV.setText("");
								accelyTV.setText("");
								accelzTV.setText("");
								gpsQualityTV.setText("");
								gyroXTV.setText("");
								gyroYTV.setText("");
								gyroZTV.setText("");
								contextValueTV.setText("");
								trueHeadingTV.setText("");
								magneticHeadingTV.setText("");

								m_aryAccel.clear();
								m_aryComp.clear();
								m_aryContext.clear();
								m_aryGps.clear();
								m_aryGyro.clear();
								m_aryRest.clear();
							}
						});
			}
		}
	};

	private OnClickListener rightListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (m_nRecordStatus) {
			case RECORD_STATUS_STOP: // Stop
				break;
			case RECORD_STATUS_REC: // Recording
				m_nRecordStatus = RECORD_STATUS_PAUSE;
				m_handlerRecord.removeCallbacks(runnableRecord);
				break;
			case RECORD_STATUS_PAUSE: // Pause
				m_nRecordStatus = RECORD_STATUS_REC;
				m_handlerRecord.postDelayed(runnableRecord, 0);
				break;
			}
			showLRButtonTitle();
		}
	};

	public void gpsUpdate() {
		if (m_bGpsOn) {
			String str = String.format("%.5f", longiValue);
			String str2 = String.format("%.5f", latiValue);
			String str3 = String.format("%.5f", altiValue);

			GpsReadings gpsReading = new GpsReadings();
			gpsReading.latitude = str2;
			gpsReading.longitude = str;
			gpsReading.altitude = str3;
			gpsReading.timeStamp = timestampformat.format(new Date());
			m_aryGps.add(gpsReading);
			longiTV.setText(str);
			latiTV.setText(str2);
			altiTV.setText(str3);
		} else {
			longiTV.setText("0");
		}
	}

	public void gyroupdate() {
		if (m_bGryroOn && gyroX != deltaX) {
			String str = String.format("%.5f", gyroX);
			String str1 = String.format("%.5f", gyroY);
			String str2 = String.format("%.5f", gyroZ);
			GyroReadings gyroReadings = new GyroReadings();
			gyroReadings.gyroXReading = str;
			gyroReadings.gyroYReading = str1;
			gyroReadings.gyroZReading = str2;
			try {
			gyroReadings.timeStamp = timestampformat.format(new Date());
			}
			catch (Exception e) {
				System.out.println("Error" + e);
			}
			m_aryGyro.add(gyroReadings);
			gyroXTV.setText(str);
			gyroYTV.setText(str1);
			gyroZTV.setText(str2);
		} else {
			gyroXTV.setText("0");
		}
	}

	public void accelupdate() {
		if (m_bAccelOn) {
			String str = String.format("%.5f", deltaX);
			String str1 = String.format("%.5f", deltaY);
			String str2 = String.format("%.5f", deltaZ);
			AccelerometerReadings accelObj = new AccelerometerReadings();
			accelObj.accelXReading = str;
			accelObj.accelYReading = str1;
			accelObj.accelZReading = str2;
			try {
			accelObj.timeStamp = timestampformat.format(new Date());
			}
			catch (Exception e) {
				System.out.println("Error" + e);
			}
			m_aryAccel.add(accelObj);
			accelxTV.setText(str);
			accelyTV.setText(str1);
			accelzTV.setText(str2);
		} else {
			accelxTV.setText("0");
		}
	}

	public void contextFind() {
		contextValueTV.setText(m_ContextValue);
		ContextObject contextObj = new ContextObject();
		contextObj.contextValue = m_ContextValue;
		try {
		contextObj.timeStamp = timestampformat.format(new Date());
		}
		catch (Exception e) {
			System.out.println("Error" + e);
		}
		m_aryContext.add(contextObj);
	}

	Date compareDate;

	public void sendviaTCP() {
		if (m_bCompletedSendViaTcp) {
			m_bCompletedSendViaTcp = false;
			new tcpAsyncTask().execute();
		}
	}

	public void restCheck() {
		// Store the previous value to file
		String vin = pref.get_prefValue(SharedPref.VIN_PREF);
		String API_url = "https://api.cloud-think.com/data/" + vin;
		// System.out.println("Checking REST");
		try {
			// System.out.println("New query set up");
			RestQuery restQueryObj = new RestQuery();
			restQueryObj.delegate = this;
			restQueryObj.execute(new String[] { API_url });
		} catch (Exception e) {
			// System.out.println("Error!");
			// System.out.println(e);
		}
	}

	public void showStartTime() {
		m_dateStartTime = new Date();
		if (m_dateStartTime != null && timestampformat != null) {
			String strStartTime = timestampformat.format(m_dateStartTime);
			startTimeTV.setText(strStartTime);
		}
	}

	public void showLRButtonTitle() {
		switch (m_nRecordStatus) {
		case RECORD_STATUS_STOP:
			leftBtn.setText("Start");
			rightBtn.setText("Pause");
			break;
		case RECORD_STATUS_REC:
			leftBtn.setText("Stop");
			rightBtn.setText("Pause");
			break;
		case RECORD_STATUS_PAUSE:
			leftBtn.setText("Stop");
			rightBtn.setText("Resume");
			break;
		default:
			leftBtn.setVisibility(View.INVISIBLE);
			rightBtn.setVisibility(View.INVISIBLE);
			break;
		}
	}

	// Duration Method............
	public void showDuration() {
		// Date duration = new Date();
		// long diff = duration.getTime() - date.getTime();
		// long timeInSeconds = diff / 1000;
		// long hours, minutes, seconds;
		// hours = timeInSeconds / 3600;
		// timeInSeconds = timeInSeconds - (hours * 3600);
		// minutes = timeInSeconds / 60;
		// timeInSeconds = timeInSeconds - (minutes * 60);
		// seconds = timeInSeconds;
		// recordDuration = (hours < 10 ? "0" + hours : hours) + ":"
		// + (minutes < 10 ? "0" + minutes : minutes) + ":"
		// + (seconds < 10 ? "0" + seconds : seconds);
		// recordDurationTV.setText(recordDuration);
		recordDuration = String.format("%02d", m_nTick / 3600) + ":"
				+ String.format("%02d", (m_nTick % 3600) / 60) + ":"
				+ String.format("%02d", (m_nTick % 3600) % 60);
		recordDurationTV.setText(recordDuration);
	}

	public void compass() {
		if (m_bCompOn) {
			if (location == null) {

			} else {
				comptrue = magnetic;

				GeomagneticField geoField = new GeomagneticField(Double
						.valueOf(location.getLatitude()).floatValue(), Double
						.valueOf(location.getLongitude()).floatValue(), Double
						.valueOf(location.getAltitude()).floatValue(),
						System.currentTimeMillis());
				comptrue -= geoField.getDeclination();

				String str = String.format("%.5f", magnetic);
				String str1 = String.format("%.5f", comptrue);

				CompassReadings compass = new CompassReadings();
				compass.mHeading = str;
				compass.tHeading = str1;
				compass.timeStamp = timestampformat.format(new Date());
				m_aryComp.add(compass);

				trueHeadingTV.setText("True: " + str);
				magneticHeadingTV.setText("Magnetic: " + str1);
			}
		} else {
			trueHeadingTV.setText("0");
			magneticHeadingTV.setText("0");
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO: Disable logging when the phone isn't actively polling sensors!
		if (isRunning) {
			switch (event.sensor.getType()) {
			case Sensor.TYPE_ACCELEROMETER:
				deltaX = event.values[0];
				deltaY = event.values[1];
				deltaZ = event.values[2];
				accelupdate();
			case Sensor.TYPE_GYROSCOPE:
				gyroX = event.values[0];
				gyroY = event.values[1];
				gyroZ = event.values[2];
				gyroupdate();
				break;
			case Sensor.TYPE_ORIENTATION:
				magnetic = event.values[0];
				compass();
				break;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (arclient != null) {
			arclient.removeActivityUpdates(pIntent);
			arclient.disconnect();
		}
		m_Context.unregisterReceiver(receiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (m_dateStartTime != null && timestampformat != null) {
			String strStartTime = timestampformat.format(m_dateStartTime);
			startTimeTV.setText(strStartTime);
		}
		showLRButtonTitle();
		m_bShowedCheckIn = false;
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		Toast.makeText(m_Context, "Connection Failed", Toast.LENGTH_SHORT)
				.show();
	}

	@Override
	public void onConnected(Bundle arg0) {
		Intent intent = new Intent(m_Context, ActivityRecognitionService.class);
		pIntent = PendingIntent.getService(m_Context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		arclient.requestActivityUpdates(1000, pIntent);
	}

	@Override
	public void onDisconnected() {
	}

	public class MyLocationListener implements LocationListener {

		@Override
		public void onLocationChanged(Location loc) {
			longiValue = loc.getLongitude();
			latiValue = loc.getLatitude();
			altiValue = loc.getAltitude();
			accuracy = loc.getAccuracy();
		}

		@Override
		public void onProviderDisabled(String provider) {
			Toast.makeText(m_Context, "GPS Disabled", Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onProviderEnabled(String provider) {
			Toast.makeText(m_Context, "GPS Enabled", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	}

	private void RecordTimer() {
		
		if (m_nTick % 20 == 0) {
			// Check
			// REST
			// every
			// 20
			// seconds
			restCheck();
		}

		if (m_nTick % 5 == 0) {
			// Check
			// m_Context
			// every
			// 5
			// seconds
			contextFind();
		}

		if (m_nTick % (1 * 60) == 0 && m_nTick != 0) {
			sendviaTCP();
		}

		if (m_nTick % (5 * 60) == 0) {
			showCheckInPage();
		}

		m_nTick++;
		showDuration();
		gpsUpdate();
		gpsQualityTV.setText(Float.toString(accuracy));
		System.out.println(m_nTick);
	}

	Runnable runnableRecord = new Runnable() {
		@Override
		public void run() {
			RecordTimer();
			if (m_handlerRecord != null) {
				m_handlerRecord.postDelayed(this, 1000);
			}
		}
	};

	class tcpAsyncTask extends AsyncTask<String, String, String> {
		Date compDate = null;
		String strTCPAddress, strTCPPort, data;

		@Override
		protected void onPreExecute() {
			// m_bCompletedSendViaTcp = false;
			compDate = new Date();
		}

		@Override
		protected String doInBackground(String... params) {
			// System.out.println("Now Date : " + compDate.toString());
			long time = compDate.getTime();
			time -= 5000;
			compDate = new Date(time);
			// System.out.println("-5 Second Date : " + compDate.toString());

			ArrayList<GpsReadings> aryGps = new ArrayList<GpsReadings>(m_aryGps);
			ArrayList<AccelerometerReadings> aryAccel = new ArrayList<AccelerometerReadings>(
					m_aryAccel);
			ArrayList<GyroReadings> aryGyro = new ArrayList<GyroReadings>(
					m_aryGyro);
			ArrayList<CompassReadings> aryComp = new ArrayList<CompassReadings>(
					m_aryComp);
			ArrayList<ContextObject> aryContext = new ArrayList<ContextObject>(
					m_aryContext);
			ArrayList<Rest> aryRest = new ArrayList<Rest>(m_aryRest);

			aryGps = (ArrayList<GpsReadings>) Predicate.filter(aryGps,
					new IPredicate<GpsReadings>() {
						@Override
						public boolean apply(GpsReadings type) {
							try {
								Date date = timestampformat
										.parse(type.timeStamp);
								return compDate.compareTo(date) < 0;
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							return false;
						}
					});

			aryAccel = (ArrayList<AccelerometerReadings>) Predicate.filter(
					aryAccel, new IPredicate<AccelerometerReadings>() {
						@Override
						public boolean apply(AccelerometerReadings type) {
							try {
								Date date = timestampformat
										.parse(type.timeStamp);
								return compDate.compareTo(date) < 0;
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							return false;
						}
					});
			aryGyro = (ArrayList<GyroReadings>) Predicate.filter(aryGyro,
					new IPredicate<GyroReadings>() {
						@Override
						public boolean apply(GyroReadings type) {
							try {
								Date date = timestampformat
										.parse(type.timeStamp);
								return compDate.compareTo(date) < 0;
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							return false;
						}
					});
			aryComp = (ArrayList<CompassReadings>) Predicate.filter(aryComp,
					new IPredicate<CompassReadings>() {
						@Override
						public boolean apply(CompassReadings type) {
							try {
								Date date = timestampformat
										.parse(type.timeStamp);
								return compDate.compareTo(date) < 0;
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							return false;
						}
					});
			aryContext = (ArrayList<ContextObject>) Predicate.filter(
					aryContext, new IPredicate<ContextObject>() {
						@Override
						public boolean apply(ContextObject type) {
							try {
								Date date = timestampformat
										.parse(type.timeStamp);
								return compDate.compareTo(date) < 0;
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							return false;
						}
					});
			aryRest = (ArrayList<Rest>) Predicate.filter(aryRest,
					new IPredicate<Rest>() {
						@Override
						public boolean apply(Rest type) {
							try {
								Date date = timestampformat
										.parse(type.timeStamp);
								return compDate.compareTo(date) < 0;
							} catch (java.text.ParseException e) {
								e.printStackTrace();
							}
							return false;
						}
					});
			
			data = "START,ANDROID";
			data += String.format("\r\nUSER,%s ",
					pref.get_prefValue(SharedPref.TCP_USERID) == null ? ""
							: pref.get_prefValue(SharedPref.TCP_USERID));
			data += "\r\nGPS\r\n";
			for (GpsReadings gpsObj : aryGps) {
				data += String.format("\r\n%s, %s, %s, %s", gpsObj.latitude,
						gpsObj.longitude, gpsObj.altitude, gpsObj.timeStamp);
			}

			// TODO: Computation of MIT algorithm here
			ArrayList<Double> AccelValues = new ArrayList<Double>();
			//data += "\r\nACCEL\r\n";
			for (AccelerometerReadings acceObj : aryAccel) {
				// Do not send all data - only the min, mean, and max
				/*
				data += String.format("\r\n%s, %s, %s, %s",
						acceObj.accelXReading, acceObj.accelYReading,
						acceObj.accelZReading, acceObj.timeStamp);
				*/
				AccelValues.add(
						Math.sqrt(
								Double.parseDouble(acceObj.accelXReading)*Double.parseDouble(acceObj.accelXReading)
								+
								Double.parseDouble(acceObj.accelYReading)*Double.parseDouble(acceObj.accelYReading)
								+
								Double.parseDouble(acceObj.accelZReading)*Double.parseDouble(acceObj.accelZReading)
						)
				);
			}

			// Acceleration metric calculation
			double AccelMin = Collections.min(AccelValues);
			double AccelMax = Collections.max(AccelValues);
			double AccelMean = 0;
			for (int i = 0; i < AccelValues.size(); i++) {
				AccelMean = AccelValues.get(i) + AccelMean;
			}
			AccelMean = AccelMean / AccelValues.size();
			data += "\r\nACCEL_STATS\r\n";
			data += String.format("\r\n%s, %s, %s",
					AccelMin, AccelMean, AccelMax);
			
			// Classification calculation
			data += "\r\nMIT_ALGO\r\n";
			/*
			int state_unknown = 1;
			int state_stationary = 2;
			int state_driving = 3;
			int state_walking = 4;
			int state_running = 5;
			int state_bicycling = 6;
			*/
			double line_between_bike_and_walk = 10.5;
			
			boolean stationary = false;
			boolean bicycling = false;
			boolean running = false;
			boolean walking = false;
			if (AccelMin > 9) { stationary = true; data += "\r\nStationary"; }
			if ( (AccelMean < line_between_bike_and_walk) && (AccelMin <= 9) ) {bicycling = true; data += "\r\nBicycling"; }
			if ( AccelMean >= 10.5 + 1.54 * AccelMin) { running = true; data += "\r\nRunning"; }
			if ( AccelMean >= line_between_bike_and_walk && running == false ) { walking = true; data += "\r\nWalking"; }
			
			// TODO: Creation of a new data type, MIT_identity, capable of storing and retrieving this data
			// As of this moment, the MIT data are uploaded but not saved to the file
			
			// Do not send these data to the server
			/*
			data += "\r\nCOMPASS\r\n";
			for (CompassReadings compObj : aryComp) {
				data += String.format("\r\n%s, %s, %s", compObj.mHeading,
						compObj.tHeading, compObj.timeStamp);
			}

			data += "\r\nGYRO\r\n";
			for (GyroReadings gyroObj : aryGyro) {
				data += String.format("\r\n%s, %s, %s, %s", gyroObj.gyroXReading,
						gyroObj.gyroYReading, gyroObj.gyroZReading,
						gyroObj.timeStamp);
			}
			*/

			data += "\r\nCONTEXT\r\n";
			for (ContextObject contextObj : aryContext) {
				data += String.format("\r\n%s, %s", contextObj.contextValue,
						contextObj.timeStamp);
			}

			data += "\r\nCLOUDTHINK\r\n";
			for (Rest restObj : aryRest) {
				data += String.format("\r\n%s, %s", restObj.restState,
						restObj.timeStamp);
			}

			data += "\r\nGROUND\r\n";
			for (Rest restObj : aryRest) {
				data += String.format("\r\n%s, %s", 
						restObj.checkin, restObj.timeStamp);
			}			
			data += "\r\nEND\r\n";

			strTCPAddress = pref.get_prefValue(SharedPref.TCP_ADDRESS);
			strTCPPort = pref.get_prefValue(SharedPref.TCP_PORT) == null ? "0"
					: pref.get_prefValue(SharedPref.TCP_PORT);
			System.out.println("Sending Data via TCP : " + data);
			// new tcpAsyncTask().execute(strTCPAddress, strTCPPort, data);
			// System.out.println("Send data via tcp every minite.");

			try {
				Socket s = new Socket(strTCPAddress,
						Integer.parseInt(strTCPPort));
				// BufferedReader in = new BufferedReader(new InputStreamReader(
				// s.getInputStream()));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						s.getOutputStream()));
				// send output msg

				out.write(data);
				out.flush();
				// Log.i("TcpClient", "sent: " + params[2]);
				// accept server response
				// String inMsg = in.readLine()
				// + System.getProperty("line.separator");
				// Log.i("TcpClient", "received: " + inMsg);
				// close connection
				s.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return "";
		}

		protected void onPostExecute(String Result) {
			m_bCompletedSendViaTcp = true;
			strTCPAddress = "";
			strTCPPort = "";
			data = "";
			super.onPostExecute(Result);
		}

		@Override
		protected void onCancelled(String result) {
			m_bCompletedSendViaTcp = true;
			strTCPAddress = "";
			strTCPPort = "";
			data = "";
			super.onCancelled(result);
		}

		@Override
		protected void onCancelled() {
			m_bCompletedSendViaTcp = true;
			strTCPAddress = "";
			strTCPPort = "";
			data = "";
			super.onCancelled();
		}

	}

}
