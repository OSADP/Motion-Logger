package com.example.mitmotionapp.setting;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class SetAccel extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText accelET;
	Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.accel, container, false);
		context = getActivity();
		pref = new SharedPref(context);
		leftBtn = (Button) view.findViewById(R.id.header_leftBtn);
		accelET = (EditText) view.findViewById(R.id.accel_ET);
		accelET.setText(pref.get_prefValue(SharedPref.ACCEL_FREQUENCY));
		accelET.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					int val = Integer.parseInt(s.toString());
					if (val > 100) {
						accelET.setText(s.subSequence(0, s.length() - 1));
					} else if (val < 1) {
						accelET.setText("1");
					}
					accelET.setSelection(accelET.getText().length());
				} catch (NumberFormatException ex) {
					// Do something
				}
			}
		});
		leftBtn.setOnClickListener(leftlistener);
		return view;
	}

	private OnClickListener leftlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String str = accelET.getText().toString();
			if (str.equalsIgnoreCase("")) {
				accelET.setHint("Takes integer values 1-100Hz");
				accelET.setError("Please enter numeric value.");
			} else {
				/* Go to next fragment in navigation stack */
				pref.save_prefValues(SharedPref.ACCEL_FREQUENCY, str);
				mActivity.pushFragments(AppConstants.TAB_C, new Settings(),
						true, true);
			}
		}
	};

}
