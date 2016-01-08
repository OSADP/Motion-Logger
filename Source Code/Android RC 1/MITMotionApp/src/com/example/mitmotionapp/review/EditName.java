package com.example.mitmotionapp.review;



import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.databases.DBAdapter;
import com.example.mitmotionapp.readingsobject.MainObject;
import com.example.mitmotionapp.readingsobject.SessionObject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class EditName extends BaseFragment {
	
	Button leftBtn;
	EditText editNameET;
	DBAdapter db;
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
        View view  =   inflater.inflate(R.layout.name, container, false);
        editNameET =	(EditText)view.findViewById(R.id.editnameET);
        leftBtn    =   (Button)view.findViewById(R.id.header_leftBtn);
        db = new DBAdapter(getActivity());
        leftBtn.setOnClickListener( leftlistener);
        return view;
    }
	private OnClickListener leftlistener        =   new View.OnClickListener(){
        @Override
        public void onClick(View v){
        	
        	String newName = editNameET.getText().toString();
        	db.updateRecordName(SessionObject.getidofNext(), newName);
            /* Go to next fragment in navigation stack*/
            mActivity.pushFragments(AppConstants.TAB_B, new Details(),true,true);
        }
    };
    public void editRecordName(MainObject mObject, String name){
    	mObject.record_name= name;
    }
}
