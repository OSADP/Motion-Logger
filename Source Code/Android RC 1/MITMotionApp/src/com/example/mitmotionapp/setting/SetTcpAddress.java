package com.example.mitmotionapp.setting;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.readingsobject.SharedPref;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SetTcpAddress extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText TcpNameET;
	Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tcp_address, container, false);
		TcpNameET = (EditText) view.findViewById(R.id.tcp_name_ET);
		context = getActivity();
		pref = new SharedPref(context);

		TcpNameET.setText(pref.get_prefValue(SharedPref.TCP_ADDRESS));
		leftBtn = (Button) view.findViewById(R.id.header_leftBtn);
		leftBtn.setOnClickListener(leftlistener);
		return view;
	}

	private OnClickListener leftlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			pref.save_prefValues(SharedPref.TCP_ADDRESS, TcpNameET.getText()
					.toString());
			/* Go to next fragment in navigation stack */
			mActivity.pushFragments(AppConstants.TAB_C, new Settings(), true,
					true);
		}
	};

}
