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

public class SetFtpPort extends BaseFragment {
	Button leftBtn;
	SharedPref pref;
	EditText portET;
	Context context;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view  =   inflater.inflate(R.layout.ftp_port, container, false);
        leftBtn  =   (Button)view.findViewById(R.id.header_leftBtn);
        portET 	=	(EditText)view.findViewById(R.id.ftp_port_ET);
        context = getActivity();
        pref = new SharedPref(context);
        leftBtn.setOnClickListener( leftlistener);
        return view;
    }
	private OnClickListener leftlistener        =   new View.OnClickListener(){
        @Override
        public void onClick(View v){
        	pref.save_prefValues(SharedPref.FTP_PORT, portET.getText().toString());
            /* Go to next fragment in navigation stack*/
            mActivity.pushFragments(AppConstants.TAB_C, new Settings(),true,true);
        }
    };

}
