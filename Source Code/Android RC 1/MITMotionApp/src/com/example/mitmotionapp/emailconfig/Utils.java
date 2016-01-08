package com.example.mitmotionapp.emailconfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import com.example.mitmotionapp.databases.DBAdapter;
import com.example.mitmotionapp.readingsobject.SessionObject;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

/**
 * Utility methods.
 * 
 * @author stephendnicholas.com
 */
public class Utils {

        /**
         * Create a new file with the given name and content, in this application's
         * cache directory.
         * 
         * @param context
         *            - Context - context to use.
         * @param fileName
         *            - String - the name of the file to create.
         * @param content
         *            - String - the content to put in the new file.
         * @throws IOException
         */
		
	   
	   
        public static void createCachedFile(Context context, String fileName) throws IOException {
        		DBAdapter db 		= new DBAdapter(context);
        		Cursor record 		= db.getRecordData(SessionObject.getidofNext());
        		Cursor gps 			= db.getGpsData(SessionObject.getidofNext());
        		Cursor accel 		= db.getAccelData(SessionObject.getidofNext());
        		Cursor compass		= db.getCompassData(SessionObject.getidofNext());
        		Cursor gyro 		= db.getGyroData(SessionObject.getidofNext());
        		Cursor contextState = db.getContextData(SessionObject.getidofNext());
        		Cursor restState		= db.getRestData(SessionObject.getidofNext());
                File cacheFile = new File(context.getCacheDir() + File.separator + fileName);

                cacheFile.createNewFile();

                FileOutputStream fos = new FileOutputStream(cacheFile);
                OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF8");
                PrintWriter pw = new PrintWriter(osw);

                pw.println("Record Name: "+"\t"+record.getString(1));
                pw.println("Record Time: "+"\t"+record.getString(2));
                pw.println("Record Duration: "+"\t"+record.getString(3));
                pw.println("GPS Quality: "+"\t"+record.getString(6));
                
                pw.println("GPS Log ..........");
                pw.println("Longitude \tLatitude \tAltitude \tTimestamp");
                
                if(gps.moveToFirst()){
                	do{
                		pw.println(gps.getString(0)+"\t"+gps.getString(1)+"\t"+gps.getString(2)+"\t"+gps.getString(3));
                	}while(gps.moveToNext());
                }
                pw.println("Acceleration Log ..........");
                pw.println("X-Axis \tY-Axis \tZ-Axis \tTimestamp");
                if(accel.moveToFirst()){
                	do{
                		pw.println(accel.getString(0)+"\t"+accel.getString(1)+"\t"+accel.getString(2)+"\t"+accel.getString(3));
                	}while(accel.moveToNext());
                }
                pw.println("Compass Log ..........");
                pw.println("Mag Heading \tTrue Heading \tTimestamp");
                if(compass.moveToFirst()){
                	do{
                		pw.println(compass.getString(0)+"\t"+"\t"+compass.getString(1)+"\t"+"\t"+compass.getString(2));
                	}while(compass.moveToNext());
                }
                pw.println("Gyroscope Log ..........");
                pw.println("X-Axis \tY-Axis \tZ-Axis \tTimestamp");
                if(gyro.moveToFirst()){
                	do{
                		pw.println(gyro.getString(0)+"\t"+gyro.getString(1)+"\t"+gyro.getString(2)+"\t"+gyro.getString(3));
                	}while(gyro.moveToNext());
                }
                pw.println("Context State Log ..........");
                pw.println("State \t Timestamp");
                if(contextState.moveToFirst()){
                	do{
                		pw.println(contextState.getString(0)+"\t"+"\t"+contextState.getString(1));
                		//System.out.println("Writing context");
                	}while(contextState.moveToNext());
                }
                pw.println("REST (CloudThink) Log ..........");
                pw.println("State \t Timestamp");
                if(restState.moveToFirst()){
                	do{
                		pw.println(restState.getString(0)+"\t"+"\t"+restState.getString(2));
                		//System.out.println("Writing REST");
                	}while(restState.moveToNext());
                }
                pw.println("Ground Truth Log ..........");
                pw.println("State \t Timestamp");
                if(restState.moveToFirst()){
                	do{
                		pw.println(restState.getString(1)+"\t"+"\t"+restState.getString(2));
                	}while(restState.moveToNext());
                }
                pw.flush();
                pw.close();
                db.close();
        }

        /**
         * Returns an intent that can be used to launch Gmail and send an email with
         * the specified file from this application's cache attached.
         * 
         * @param context
         *            - Context - the context to use.
         * @param email
         *            - String - the 'to' email address.
         * @param subject
         *            - String - the email subject.
         * @param body
         *            - String - the email body.
         * @param fileName
         *            - String - the name of the file in this application's cache to
         *            attach to the email.
         * @return An Intent that can be used to launch the Gmail composer with the
         *         specified file attached.
         */
        public static Intent getSendEmailIntent(Context context, String email,
                        String subject, String body, String fileName) {

                final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                // Explicitly only use Gmail to send
                emailIntent.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");

                emailIntent.setType("plain/text");

                // Add the recipients
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { email });

                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

                emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);

                // Add the attachment by specifying a reference to our custom
                // ContentProvider
                // and the specific file of interest
                emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + CachedFileProvider.AUTHORITY + "/"  + fileName));

                return emailIntent;
        }
}