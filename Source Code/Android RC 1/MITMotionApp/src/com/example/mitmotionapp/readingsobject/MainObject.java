package com.example.mitmotionapp.readingsobject;

import java.text.SimpleDateFormat;
import java.util.Locale;



public class MainObject{
	
	public String record_name;
	public String record_time;
	public String record_duration;
	public String gpsAccuraccy;
	public int gpsSamples,accelSamples,compassSamples,gyroSamples;
	public int isGpsOn;
	public int isAccelOn;
	public int isGyroOn;
	public int isCompassOn;
	
	
	
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
	

	public int gpsStatus(){
		return isGpsOn;
	}

	public int accelStatus(){
		return isAccelOn;
	}

	public int gyroStatus(){
		return isGyroOn;
	}

	public int compStatus(){
		return isCompassOn;
	}
}
