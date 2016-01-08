package com.example.mitmotionapp.review;

import it.sauronsoftware.ftp4j.FTPClient;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.databases.DBAdapter;
import com.example.mitmotionapp.emailconfig.Utils;
import com.example.mitmotionapp.readingsobject.MainObject;
import com.example.mitmotionapp.readingsobject.SessionObject;
import com.example.mitmotionapp.readingsobject.SharedPref;

public class Details extends BaseFragment {

	Button leftBtn, emailBTN, ftpBTN, tcpBtn;
	ImageView editNameIV, gpsIndc, accelIndc, compIndc, gyroIndc;
	TextView recordNameTV, recordTimeTV, recordDurationTV;
	String result = "file upload";
	String FTP_HOST = "23.23.126.78";
	String FTP_USER = null;
	String FTP_PASS = null;

	String TCP_ADDRESS = null;
	int TCP_PORT;
	String TCP_USERID = null;
	String m_strData = "";
	MainObject mObj;
	public SimpleDateFormat dateFormat;
	SharedPref pref;
	DBAdapter db;
	int gpsStatus, accelStatus, gyroStatus, compassStatus;
	String record_name, record_time, record_duration, gpsAccuracy;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.details, container, false);
		// System.out.println(SessionObject.getidofNext());

		recordNameTV = (TextView) view.findViewById(R.id.record_nameTV);
		recordTimeTV = (TextView) view.findViewById(R.id.record_timeTV);
		recordDurationTV = (TextView) view.findViewById(R.id.record_durationTV);

		db = new DBAdapter(getActivity());

		mObj = new MainObject();
		pref = new SharedPref(getActivity());
		FTP_HOST = pref.get_prefValue(SharedPref.FTP_NAME);
		FTP_USER = pref.get_prefValue(SharedPref.FTP_USER);
		FTP_PASS = pref.get_prefValue(SharedPref.FTP_PASS);

		Cursor c = db.getRecordData(SessionObject.getidofNext());
		record_name = c.getString(1);
		record_time = c.getString(2);
		record_duration = c.getString(3);
		gpsAccuracy = c.getString(8);

		recordNameTV.setText(record_name);
		recordTimeTV.setText(record_time);
		recordDurationTV.setText(record_duration);

		gpsStatus = c.getInt(4);
		accelStatus = c.getInt(5);
		gyroStatus = c.getInt(6);
		compassStatus = c.getInt(7);

		leftBtn = (Button) view.findViewById(R.id.header_leftBtn);
		editNameIV = (ImageView) view.findViewById(R.id.name_nextIV);
		gpsIndc = (ImageView) view.findViewById(R.id.gps_indcIV);
		accelIndc = (ImageView) view.findViewById(R.id.accel_indcIV);
		gyroIndc = (ImageView) view.findViewById(R.id.gyro_indcIV);
		compIndc = (ImageView) view.findViewById(R.id.comp_indcIV);
		ftpBTN = (Button) view.findViewById(R.id.ftpbtn);
		emailBTN = (Button) view.findViewById(R.id.emailbtn);
		tcpBtn = (Button) view.findViewById(R.id.tcpbtn);

		if (gpsStatus == 0) {
			gpsIndc.setImageResource(R.drawable.blackindc);
		}
		if (accelStatus == 0) {
			accelIndc.setImageResource(R.drawable.blackindc);
		}
		if (gyroStatus == 0) {
			gyroIndc.setImageResource(R.drawable.blackindc);
		}
		if (compassStatus == 0) {
			compIndc.setImageResource(R.drawable.blackindc);
		}
		leftBtn.setOnClickListener(leftlistener);
		editNameIV.setOnClickListener(eNamelistener);
		emailBTN.setOnClickListener(emaillistener);
		ftpBTN.setOnClickListener(ftplistener);
		tcpBtn.setOnClickListener(tcplistener);

		TCP_ADDRESS = pref.get_prefValue(SharedPref.TCP_ADDRESS);
		TCP_PORT = Integer
				.parseInt(pref.get_prefValue(SharedPref.TCP_PORT) == null ? "0"
						: pref.get_prefValue(SharedPref.TCP_PORT));

		// try {
		// Socket socket = new Socket("asdf", 1231);
		// OutputStream out = socket.getOutputStream();
		// PrintWriter output = new PrintWriter(out);
		//
		// } catch (UnknownHostException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		return view;
	}

	private OnClickListener leftlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			/* Go to next fragment in navigation stack */
			mActivity.pushFragments(AppConstants.TAB_B, new Review(), true,
					true);
		}
	};
	private OnClickListener eNamelistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			/* Go to next fragment in navigation stack */
			mActivity.pushFragments(AppConstants.TAB_B, new EditName(), true,
					true);
		}
	};
	private OnClickListener emaillistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			try {
				// Write a dummy text file to this application's internal
				// cache dir.
				Utils.createCachedFile(getActivity(), "SensorValues.txt");

				// Then launch the activity to send that file via gmail.
				startActivity(Utils
						.getSendEmailIntent(
								getActivity(),
								pref.get_prefValue(SharedPref.EMAIL_ADDRESS),
								"Subject: Recording Log",
								"Please see attached log file containing sensor and context information.",
								"SensorValues.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Catch if GMail is not available on this device
			catch (ActivityNotFoundException e) {
				Toast.makeText(getActivity(),
						"GMail is not available on this device.",
						Toast.LENGTH_SHORT).show();
			}
			mActivity.pushFragments(AppConstants.TAB_B, new Details(), true,
					true);
		}
	};

	private OnClickListener ftplistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			createFTPFile();

			new ftpAsyncTask().execute();
		}
	};

	private OnClickListener tcplistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			createFTPFile();
			File file = new File(getActivity().getFilesDir() + "/"
					+ record_name + ".txt");
			try {
				FileInputStream fin = new FileInputStream(file);
				byte[] buffer = new byte[(int) file.length()];
				new DataInputStream(fin).readFully(buffer);
				fin.close();
				m_strData = new String(buffer, "UTF-8");
				// System.out.println(m_strData);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (m_strData != null) {
				new tcpAsyncTask().execute();
			}
		}
	};

	// asyncTask class started
	class ftpAsyncTask extends AsyncTask<String, Void, String> {
		ProgressDialog progresdialoglistview;

		@Override
		protected void onPreExecute() {
			progresdialoglistview = ProgressDialog.show(getActivity(), "",
					"Loading");
//			Log.e("onPreExecutive", "called" + progresdialoglistview);

		}

		@Override
		protected String doInBackground(String... arg0) {

			File file = new File(getActivity().getFilesDir() + "/"
					+ record_name + ".txt");

			FTPClient client = new FTPClient();
			try {

				System.out.println("KK" + FTP_HOST);
				System.out.println("KK" + FTP_USER);
				System.out.println("KK" + FTP_PASS);
				client.connect(FTP_HOST, 21);
				client.login(FTP_USER, FTP_PASS);
				client.setType(FTPClient.TYPE_BINARY);
				client.upload(file);
			} catch (Exception e) {
				e.printStackTrace();
				try {
					client.disconnect(true);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				return null;
			}

			return "";
		}

		protected void onPostExecute(String Result) {
			// System.out.println(Result);
			progresdialoglistview.dismiss();
			String result = (Result != null) ? "Uploading Completed"
					: "Uploading Failed";
			Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
		}
	}

	class tcpAsyncTask extends AsyncTask<String, Void, String> {
		ProgressDialog progresdialoglistview;

		@Override
		protected void onPreExecute() {
			progresdialoglistview = ProgressDialog.show(getActivity(), "",
					"Loading");
			Log.e("onPreExecutive", "called" + progresdialoglistview);

		}

		@Override
		protected String doInBackground(String... arg0) {
			try {
				// System.out.println("KK"+TCP_ADDRESS);
				// System.out.println("KK"+TCP_PORT);
				Socket s = new Socket(TCP_ADDRESS, TCP_PORT);
				// BufferedReader in = new BufferedReader(new InputStreamReader(
				// s.getInputStream()));
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						s.getOutputStream()));
				// send output msg

				out.write(m_strData);
				out.flush();
				Log.i("TcpClient", "sent: " + m_strData);
				// accept server response
				// String inMsg = in.readLine()
				// + System.getProperty("line.separator");
				// Log.i("TcpClient", "received: " + inMsg);
				// close connection
				s.close();
			} catch (UnknownHostException e) {
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
			return result;
		}

		protected void onPostExecute(String Result) {
			progresdialoglistview.dismiss();
			String result = (Result != null) ? "Uploading Completed"
					: "Uploading Failed";
			Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
		}

	}

	public void createFTPFile() {
		Cursor gps = db.getGpsData(SessionObject.getidofNext());
		Cursor accel = db.getAccelData(SessionObject.getidofNext());
		Cursor compass = db.getCompassData(SessionObject.getidofNext());
		Cursor gyro = db.getGyroData(SessionObject.getidofNext());
		Cursor contextState = db.getContextData(SessionObject.getidofNext());
		Cursor restState = db.getRestData(SessionObject.getidofNext());
		try {

			FileOutputStream fOut = getActivity().openFileOutput(
					record_name + ".txt", Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut, "UTF8");
			PrintWriter pw = new PrintWriter(osw, false); // Auto-buffer turned
															// off to speed up
															// writing
			// This may well be the bottleneck in data logging

			pw.println("Record Name: " + "\t" + record_name);
			pw.println("Record Time: " + "\t" + record_time);
			pw.println("Record Duration: " + "\t" + record_duration);
			pw.println("GPS Quality: " + "\t" + gpsAccuracy);
			pw.println("GPS Reading Log ..........");
			pw.println("Longitude \tLatitude \tAltitude \tTimestamp");
			if (gps.moveToFirst()) {
				do {
					pw.println(gps.getString(0) + "\t" + "\t"
							+ gps.getString(1) + "\t" + "\t" + gps.getString(2)
							+ "\t" + "\t" + gps.getString(3));
				} while (gps.moveToNext());
			}
			pw.println("Acceleration Log ..........");
			pw.println("X-Axis \tY-Axis \tZ-Axis \tTimestamp");
			if (accel.moveToFirst()) {
				do {
					pw.println(accel.getString(0) + "\t" + "\t"
							+ accel.getString(1) + "\t" + "\t"
							+ accel.getString(2) + "\t" + "\t"
							+ accel.getString(3));
				} while (accel.moveToNext());
			}
			pw.println("Compass Log ..........");
			pw.println("Mag Heading \tTrue Heading \tTimestamp");
			if (compass.moveToFirst()) {
				do {
					pw.println(compass.getString(0) + "\t" + "\t"
							+ compass.getString(1) + "\t" + "\t"
							+ compass.getString(2));
				} while (compass.moveToNext());
			}
			pw.println("Gyroscope Log ..........");
			pw.println("X-Axis \tY-Axis \tZ-Axis \tTimestamp");
			if (gyro.moveToFirst()) {
				do {
					pw.println(gyro.getString(0) + "\t" + "\t"
							+ gyro.getString(1) + "\t" + "\t"
							+ gyro.getString(2) + "\t" + "\t"
							+ gyro.getString(3));
				} while (gyro.moveToNext());
			}
			pw.println("Context State Log ..........");
			pw.println("State \t Timestamp");
			if (contextState.moveToFirst()) {
				do {
					pw.println(contextState.getString(0) + "\t" + "\t"
							+ contextState.getString(1));
					// System.out.println("Writing context");
				} while (contextState.moveToNext());
			}
            pw.println("REST (CloudThink) Log ..........");
            pw.println("State \t Timestamp");
            if(restState.moveToFirst()){
            	do{
            		pw.println(restState.getString(0)+"\t"+"\t"+restState.getString(2));
            		//System.out.println("Writing REST");
            	}while(restState.moveToNext());
            }
            pw.println("Ground Truth Log ..........");
            pw.println("State \t Timestamp");
            if(restState.moveToFirst()){
            	do{
            		pw.println(restState.getString(1)+"\t"+"\t"+restState.getString(2));
            	}while(restState.moveToNext());
            }
			pw.flush();
			pw.close();
			// System.out.print("File successfully written.");
			db.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		db.close();

	}

}
