package com.example.mitmotionapp.readingsobject;

public class SessionObject {
		
		public static int nexttag;
		
		//Set methods............
		public static void setIdforNext(int tag){
			nexttag = tag;
		}
	
		
		//get methods............
		public static int getidofNext(){
			return nexttag;
		}
	
}
