package com.example.mitmotionapp.setting;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.readingsobject.SharedPref;

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

public class SetGyro extends BaseFragment {
	Button leftBtn;
	Context context;
	SharedPref pref;
	EditText gyroET;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view  	=   inflater.inflate(R.layout.gyro, container, false);
        leftBtn  	=   (Button)view.findViewById(R.id.header_leftBtn);
        gyroET 		=	(EditText)view.findViewById(R.id.gyro_ET);
        context 	= 	getActivity();
        pref 		= 	new SharedPref(context);
        gyroET.setText(pref.get_prefValue(SharedPref.GYRO_FREQUENCY));
        gyroET.addTextChangedListener(new TextWatcher() {

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
						gyroET.setText(s.subSequence(0, s.length() - 1));
					} else if (val < 1) {
						gyroET.setText("1");
					}
					gyroET.setSelection(gyroET.getText().length());
				} catch (NumberFormatException ex) {
					// Do something
				}
			}
		});
        leftBtn.setOnClickListener( leftlistener);
        return view;
    }
	private OnClickListener leftlistener        =   new View.OnClickListener(){
        @Override
        public void onClick(View v){
        	String str = gyroET.getText().toString();
        	if (str.equalsIgnoreCase(""))
        	{
        		gyroET.setHint("Takes integer values 1-100Hz");
        		gyroET.setError("Please enter numeric value.");
        	}
        	else 
        	{
                /* Go to next fragment in navigation stack*/
        		pref.save_prefValues(SharedPref.GYRO_FREQUENCY, str);
                mActivity.pushFragments(AppConstants.TAB_C, new Settings(),true,true);     		
        	}
        }
    };

}
