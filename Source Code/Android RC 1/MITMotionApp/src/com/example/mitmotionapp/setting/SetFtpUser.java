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

public class SetFtpUser extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText userET;
	Context context;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view  =   inflater.inflate(R.layout.ftp_user, container, false);
        leftBtn  =   (Button)view.findViewById(R.id.header_leftBtn);
        userET 	=	(EditText)view.findViewById(R.id.ftp_user_ET);
        context = getActivity();
        pref = new SharedPref(context);
        
        userET.setText(pref.get_prefValue(SharedPref.FTP_USER));
        leftBtn.setOnClickListener( leftlistener);
        return view;
    }
	private OnClickListener leftlistener        =   new View.OnClickListener(){
		
        @Override
        public void onClick(View v){
        	pref.save_prefValues(SharedPref.FTP_USER, userET.getText().toString());
            /* Go to next fragment in navigation stack*/
            mActivity.pushFragments(AppConstants.TAB_C, new Settings(),true,true);
        }
    };

}
