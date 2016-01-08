package com.example.mitmotionapp.setting;

import android.content.Context;
import android.os.Bundle;
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

public class SetEmailAdress extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText emailET;
	Context context;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view  =   inflater.inflate(R.layout.email, container, false);
        leftBtn  =   (Button)view.findViewById(R.id.header_leftBtn);
        emailET 	=	(EditText)view.findViewById(R.id.accel_ET);
        context = getActivity();
        pref = new SharedPref(context);
        leftBtn.setOnClickListener( leftlistener);
        return view;
    }
	private OnClickListener leftlistener        =   new View.OnClickListener(){
        @Override
        public void onClick(View v){
            /* Go to next fragment in navigation stack*/
        	pref.save_prefValues(SharedPref.EMAIL_ADDRESS, emailET.getText().toString());
            mActivity.pushFragments(AppConstants.TAB_C, new Settings(),true,true);
        }
    };

}
