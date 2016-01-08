package com.example.mitmotionapp.setting;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.readingsobject.SharedPref;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

public class Settings extends BaseFragment {
	RelativeLayout gps_rl, gyro_rl, comp_rl, vin_rl, accel_rl, ftpName_rl,
			ftpUser_rl, ftpPort_rl, ftpPass_rl, tcpAddress_rl, tcpPort_rl,
			tcpUserID_rl, email_rl;
	SharedPref pref;
	TextView gpsFreq_ValueTV, accelFreq_ValueTV, compassFreq_ValueTV,
			gyroFreq_ValueTV, ftpName_ValueTV, ftpUser_ValueTV,
			ftpPass_ValueTV, ftpPort_ValueTV, email_ValueTV,
			tcpAddress_ValueTV, tcpPort_ValueTV, tcpUserID_ValueTV,
			vin_ValueTV;

	Switch gpsSwitch, accelSwitch, gyroSwitch, compassSwitch;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.settings, container, false);

		gps_rl = (RelativeLayout) view.findViewById(R.id.settings_rl_gps);
		gyro_rl = (RelativeLayout) view.findViewById(R.id.settings_rl_gyro);
		accel_rl = (RelativeLayout) view.findViewById(R.id.settings_rl_acce);
		comp_rl = (RelativeLayout) view.findViewById(R.id.settings_rl_comp);
		vin_rl = (RelativeLayout) view.findViewById(R.id.settings_rl_vin);
		ftpName_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_ftpname);
		ftpUser_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_ftpuser);
		ftpPass_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_ftppass);
		ftpPort_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_ftpport);
		tcpAddress_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_tcpaddress);
		tcpPort_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_tcpport);
		tcpUserID_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_tcpuserid);
		email_rl = (RelativeLayout) view
				.findViewById(R.id.settings_rl_emailadd);

		gpsFreq_ValueTV = (TextView) view.findViewById(R.id.gps_TV);
		accelFreq_ValueTV = (TextView) view.findViewById(R.id.accel_freqTV);
		gyroFreq_ValueTV = (TextView) view.findViewById(R.id.gyro_freqTV);
		compassFreq_ValueTV = (TextView) view.findViewById(R.id.compass_freqTV);
		vin_ValueTV = (TextView) view.findViewById(R.id.vinnumberTV);
		ftpName_ValueTV = (TextView) view.findViewById(R.id.ftpName_ValueTV);
		ftpUser_ValueTV = (TextView) view.findViewById(R.id.ftpUser_ValueTV);
		ftpPass_ValueTV = (TextView) view.findViewById(R.id.ftpPass_ValueTV);
		ftpPort_ValueTV = (TextView) view.findViewById(R.id.ftpPort_ValueTV);
		email_ValueTV = (TextView) view.findViewById(R.id.email_ValueTV);
		tcpAddress_ValueTV = (TextView) view.findViewById(R.id.tcpName_ValueTV);
		tcpPort_ValueTV = (TextView) view.findViewById(R.id.tcpport_ValueTV);
		tcpUserID_ValueTV = (TextView) view.findViewById(R.id.tcpuser_ValueTV);

		gpsSwitch = (Switch) view.findViewById(R.id.gps_switch);
		accelSwitch = (Switch) view.findViewById(R.id.accel_switch);
		gyroSwitch = (Switch) view.findViewById(R.id.gyro_switch);
		compassSwitch = (Switch) view.findViewById(R.id.comp_switch);

		pref = new SharedPref(getActivity());

		// gps_rl.setOnClickListener(onClickListener);
		gyro_rl.setOnClickListener(onClickListener);
		vin_rl.setOnClickListener(onClickListener);
		accel_rl.setOnClickListener(onClickListener);
		comp_rl.setOnClickListener(onClickListener);
		ftpName_rl.setOnClickListener(onClickListener);
		ftpUser_rl.setOnClickListener(onClickListener);
		ftpPass_rl.setOnClickListener(onClickListener);
		ftpPort_rl.setOnClickListener(onClickListener);
		tcpAddress_rl.setOnClickListener(onClickListener);
		tcpPort_rl.setOnClickListener(onClickListener);
		tcpUserID_rl.setOnClickListener(onClickListener);
		email_rl.setOnClickListener(onClickListener);

		gpsSwitch.setOnCheckedChangeListener(SwitchListener);
		accelSwitch.setOnCheckedChangeListener(SwitchListener);
		gyroSwitch.setOnCheckedChangeListener(SwitchListener);
		compassSwitch.setOnCheckedChangeListener(SwitchListener);
		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		setAllTextOfColomn();
	}

	private void setAllTextOfColomn() {

		setGpsText();
		accelFreq_ValueTV.setText(pref
				.get_prefValue(SharedPref.ACCEL_FREQUENCY) == null ? "0 Hz"
				: pref.get_prefValue(SharedPref.ACCEL_FREQUENCY) + " Hz");
		gyroFreq_ValueTV
				.setText(pref.get_prefValue(SharedPref.GYRO_FREQUENCY) == null ? "0 Hz"
						: pref.get_prefValue(SharedPref.GYRO_FREQUENCY) + " Hz");
		compassFreq_ValueTV.setText(pref
				.get_prefValue(SharedPref.COMPASS_FREQUENCY) == null ? "0 Hz"
				: pref.get_prefValue(SharedPref.COMPASS_FREQUENCY) + " Hz");

		vin_ValueTV.setText(pref.get_prefValue(SharedPref.VIN_PREF));
		ftpName_ValueTV.setText(pref.get_prefValue(SharedPref.FTP_NAME));
		ftpUser_ValueTV.setText(pref.get_prefValue(SharedPref.FTP_USER));
		String sPwd = pref.get_prefValue(SharedPref.FTP_PASS) == null ? ""
				: pref.get_prefValue(SharedPref.FTP_PASS);
		String str = "";
		for (int i = 0; i < sPwd.length(); i++) {
			str += "*";
		}
		ftpPass_ValueTV.setText(str);
		ftpPort_ValueTV.setText(pref.get_prefValue(SharedPref.FTP_PORT));

		tcpAddress_ValueTV.setText(pref.get_prefValue(SharedPref.TCP_ADDRESS));
		tcpPort_ValueTV.setText(pref.get_prefValue(SharedPref.TCP_PORT));
		tcpUserID_ValueTV.setText(pref.get_prefValue(SharedPref.TCP_USERID));

		email_ValueTV.setText(pref.get_prefValue(SharedPref.EMAIL_ADDRESS));

		// Switch Button check status........
		gpsSwitch
				.setChecked(pref.get_prefValue(SharedPref.GPS_SWITCH) == null ? true
						: pref.get_prefValue(SharedPref.GPS_SWITCH).equals("1"));
		accelSwitch
				.setChecked(pref.get_prefValue(SharedPref.ACCEL_SWITCH) == null ? true
						: pref.get_prefValue(SharedPref.ACCEL_SWITCH).equals(
								"1"));
		compassSwitch
				.setChecked(pref.get_prefValue(SharedPref.COMPASS_SWITCH) == null ? true
						: pref.get_prefValue(SharedPref.COMPASS_SWITCH).equals(
								"1"));
		gyroSwitch
				.setChecked(pref.get_prefValue(SharedPref.GYRO_SWITCH) == null ? true
						: pref.get_prefValue(SharedPref.GYRO_SWITCH)
								.equals("1"));

	}

	private void setGpsText() {
		gpsFreq_ValueTV.setText(gpsSwitch.isChecked() ? "Always On"
				: "Context Only");
	}

	private OnCheckedChangeListener SwitchListener = new OnCheckedChangeListener() {

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			if (buttonView == gpsSwitch) {
				pref.save_prefValues(SharedPref.GPS_SWITCH, isChecked ? "1"
						: "0");
				setGpsText();
			} else if (buttonView == accelSwitch) {
				pref.save_prefValues(SharedPref.ACCEL_SWITCH, isChecked ? "1"
						: "0");
			} else if (buttonView == gyroSwitch) {
				pref.save_prefValues(SharedPref.GYRO_SWITCH, isChecked ? "1"
						: "0");
			} else if (buttonView == compassSwitch) {
				pref.save_prefValues(SharedPref.COMPASS_SWITCH, isChecked ? "1"
						: "0");
			}
		}
	};

	private OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			/* Go to next fragment in navigation stack */
			if (v == gps_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetGps(), true,
						true);
			} else if (v == gyro_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetGyro(),
						true, true);
			} else if (v == vin_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetVin(), true,
						true);
			} else if (v == accel_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetAccel(),
						true, true);
			} else if (v == comp_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetComp(),
						true, true);
			} else if (v == ftpName_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetFtpName(),
						true, true);
			} else if (v == ftpUser_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetFtpUser(),
						true, true);
			} else if (v == ftpPass_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetFtpPass(),
						true, true);
			} else if (v == ftpPort_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetFtpPort(),
						true, true);
			} else if (v == email_rl) {
				mActivity.pushFragments(AppConstants.TAB_C,
						new SetEmailAdress(), true, true);
			} else if (v == tcpAddress_rl) {
				mActivity.pushFragments(AppConstants.TAB_C,
						new SetTcpAddress(), true, true);
			} else if (v == tcpPort_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetTcpPort(),
						true, true);
			} else if (v == tcpUserID_rl) {
				mActivity.pushFragments(AppConstants.TAB_C, new SetTcpUserid(),
						true, true);
			}
		}
	};

}