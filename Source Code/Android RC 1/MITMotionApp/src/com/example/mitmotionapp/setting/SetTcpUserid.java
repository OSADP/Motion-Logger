package com.example.mitmotionapp.setting;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.readingsobject.SharedPref;

public class SetTcpUserid extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText TcpUserET;
	Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.tcp_user, container, false);
		context = getActivity();
		pref = new SharedPref(context);
		leftBtn = (Button) view.findViewById(R.id.header_leftBtn);
		TcpUserET = (EditText) view.findViewById(R.id.tcp_user_ET);
		TcpUserET.setText(pref.get_prefValue(SharedPref.TCP_USERID));
		leftBtn.setOnClickListener(leftlistener);
		return view;
	}

	private OnClickListener leftlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String str = TcpUserET.getText().toString();
				/* Go to next fragment in navigation stack */
				pref.save_prefValues(SharedPref.TCP_USERID, str);
				mActivity.pushFragments(AppConstants.TAB_C, new Settings(),
						true, true);
		}
	};

}
