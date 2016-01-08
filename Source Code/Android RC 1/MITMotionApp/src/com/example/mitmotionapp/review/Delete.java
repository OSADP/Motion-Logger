package com.example.mitmotionapp.review;

import java.text.SimpleDateFormat;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mitmotionapp.AppConstants;
import com.example.mitmotionapp.BaseFragment;
import com.example.mitmotionapp.R;
import com.example.mitmotionapp.databases.DBAdapter;
import com.example.mitmotionapp.readingsobject.MainObject;
import com.example.mitmotionapp.readingsobject.SessionObject;
import com.example.mitmotionapp.readingsobject.SharedPref;

public class Delete extends BaseFragment {
	
	Button leftBtn;
	ImageView editNameIV;
	TextView recordNameTV;
	ProgressDialog  progresdialoglistview;
	MainObject mObj ;
	public SimpleDateFormat dateFormat;
	SharedPref pref;
	DBAdapter db;
	String record_name;
	
	  @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
	        View view = inflater.inflate(R.layout.delete, container, false);
//	        System.out.println(SessionObject.getidofNext());
	        
	        recordNameTV 		=	(TextView)view.findViewById(R.id.record_nameTV);
	        db					= 	new DBAdapter(getActivity());
	        mObj 				= 	new MainObject();
	        pref				= 	new SharedPref(getActivity());	        
	        //Cursor c = db.getRecordData(SessionObject.getidofNext());
	        db.deleteRecordData(SessionObject.getidofNext());
	        //recordNameTV.setText(record_name);

	        leftBtn  			=   (Button)view.findViewById(R.id.header_leftBtn);
	        editNameIV  		=   (ImageView)view.findViewById(R.id.name_nextIV);

	        leftBtn.setOnClickListener( leftlistener);
	        
	        db.close();
	        
	        return view;
	  }   
	  
	  private OnClickListener leftlistener        =   new View.OnClickListener(){
	        @Override
	        public void onClick(View v){
	            /* Go to next fragment in navigation stack*/
	            mActivity.pushFragments(AppConstants.TAB_B, new Review(),true,true);
	        }
	    };
	}

