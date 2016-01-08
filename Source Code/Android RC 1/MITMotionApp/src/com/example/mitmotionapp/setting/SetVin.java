package com.example.mitmotionapp.setting;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.readingsobject.SharedPref;

public class SetVin extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText vinET;
	Context context;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vinnumber, container, false);
		leftBtn = (Button) view.findViewById(R.id.header_leftBtn);
		vinET = (EditText) view.findViewById(R.id.vin_ET);
		context = getActivity();
		pref = new SharedPref(context);
		vinET.setText(pref.get_prefValue(SharedPref.VIN_PREF));
		vinET.addTextChangedListener(new TextWatcher() {

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
				if (s.length() > 17) {
					vinET.setText(s.subSequence(0, 17));
				}
				vinET.setSelection(vinET.getText().length());
			}
		});

		leftBtn.setOnClickListener(leftlistener);
		return view;
	}

	private OnClickListener leftlistener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			String str = vinET.getText().toString();
			if (str.equalsIgnoreCase("") || str.length() != 17) {
				vinET.setHint("Takes 17-char VIN");
				vinET.setError("Please enter a valid VIN");
			} else {
				/* Go to next fragment in navigation stack */
				pref.save_prefValues(SharedPref.VIN_PREF, str);
				mActivity.pushFragments(AppConstants.TAB_C, new Settings(),
						true, true);
			}
		}
	};

}
