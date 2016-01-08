package com.example.mitmotionapp.records;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.mitmotionapp.BaseFragment;
import com.jsiegel.mitmotionapp.R;
import com.example.mitmotionapp.readingsobject.SharedPref;

public class Checkin extends BaseFragment {
	SharedPref pref;
	Context context;
	Button m_btnWalking, m_btnBus, m_btnRunning, m_btnAirplane, m_btnCycling,
			m_btnTrain, m_btnDriving, m_btnBoat, m_btnStationary;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.checkin, container, false);
		context = getActivity();
		pref = new SharedPref(context);

		m_btnWalking = (Button) view.findViewById(R.id.checkin_btn_walking);
		m_btnBus = (Button) view.findViewById(R.id.checkin_btn_bus);
		m_btnRunning = (Button) view.findViewById(R.id.checkin_btn_running);
		m_btnAirplane = (Button) view.findViewById(R.id.checkin_btn_airplane);
		m_btnCycling = (Button) view.findViewById(R.id.checkin_btn_cycling);
		m_btnTrain = (Button) view.findViewById(R.id.checkin_btn_train);
		m_btnDriving = (Button) view.findViewById(R.id.checkin_btn_driving);
		m_btnBoat = (Button) view.findViewById(R.id.checkin_btn_boat);
		m_btnStationary = (Button) view
				.findViewById(R.id.checkin_btn_stationary);

		m_btnWalking.setOnClickListener(onClickListener);
		m_btnBus.setOnClickListener(onClickListener);
		m_btnRunning.setOnClickListener(onClickListener);
		m_btnAirplane.setOnClickListener(onClickListener);
		m_btnCycling.setOnClickListener(onClickListener);
		m_btnTrain.setOnClickListener(onClickListener);
		m_btnDriving.setOnClickListener(onClickListener);
		m_btnBoat.setOnClickListener(onClickListener);
		m_btnStationary.setOnClickListener(onClickListener);

		return view;
	}

	private OnClickListener onClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			String[] strCheckins = { "Walking", "Bus", "Running", "Airplane",
					"Cycling", "Train", "Driving", "Boat", "Stationary" };
			int nTag = Integer.parseInt(v.getTag().toString());
			if (strCheckins.length > nTag)
			{
				pref.save_prefValues(SharedPref.CHECKIN_TYPE, strCheckins[nTag]);
			}
			System.out.println(strCheckins[nTag]);
			mActivity.popFragments();
		}
	};
}
