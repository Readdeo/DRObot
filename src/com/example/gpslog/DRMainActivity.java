package com.example.gpslog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DRMainActivity extends Activity {

	TextView textViewLat, textViewLon, textViewGPSTime, textViewAcc,
			textViewSpeed, textViewAlt, textViewBearing, textViewSensor,
			textViewX;

	static TextView batteryPercent;

	TextView textView12;

	TextView textView13;

	Button gombStart, gombStop, gomb, gombNetStart, gombNetStop, button;

	 Location location = null;

	 private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
		    @Override
		    public void onReceive(Context arg0, Intent intent) {
		      // TODO Auto-generated method stub
		      int level = intent.getIntExtra("level", 0);
		      batteryPercent.setText(String.valueOf(level) + "%");
		    }
		  };

    	

	 
	

	// OnCreate-------------------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	
		LogToFile("DRMainActivity elindult");
		
		this.registerReceiver(this.mBatInfoReceiver, 
			    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		gombStart = (Button) findViewById(R.id.button1);
		gombStop = (Button) findViewById(R.id.button2);
		gomb = (Button) findViewById(R.id.button3);
		
		textViewLat = (TextView) findViewById(R.id.textView1);
		textViewLon = (TextView) findViewById(R.id.textView2);
		textViewGPSTime = (TextView) findViewById(R.id.textView3);
		textViewBearing = (TextView) findViewById(R.id.textView4);
		textViewSpeed = (TextView) findViewById(R.id.textView5);
		textViewAlt = (TextView) findViewById(R.id.textView6);
		textViewAcc = (TextView) findViewById(R.id.textView7);
		textViewX = (TextView) findViewById(R.id.textView10);
		batteryPercent = (TextView) findViewById(R.id.textView11);
		textView12 = (TextView) findViewById(R.id.textView12);
		textView13 = (TextView) findViewById(R.id.textView13);
		
		final Intent myIntent = new Intent(this, DataLogService.class);
		final Intent ServerIntent = new Intent(this, DRServer.class);


		startService(ServerIntent);
	
//	String fileStr = Environment.getExternalStorageDirectory() + "/DataLog.kml";
//	String LineToRemove = "</coordinates> </LineString> </MultiGeometry> </Placemark> </Folder> </Document> </kml>";
	
	gomb.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			new doFileUploa().execute();
			new doFileUpload2().execute();
			
			double currentLat = 46.860;
		    double currentLng = 17.440;

		    double destLat = 46.850;
		    double destLng = 17.430;

		    final float[] results= new float[3];
		    Location.distanceBetween(currentLat, currentLng, destLat, destLng, results);
		    Log.d("GPS", "results[0]: " + results[0]);
		    Log.d("GPS", "results[1]: " + results[1]);
		    Log.d("GPS", "results[2]: " + results[2]);

	/**	    
		    Location here = new Location("Current");
		    here.setLatitude(currentLat);
		    here.setLatitude(currentLng);

		    Location dest = new Location("Destination2");
		    dest.setLongitude(destLat);
		    dest.setLatitude(destLng);

		    Log.d("GPS", "Bearing to dest: " + here.bearingTo(dest));
		    Log.d("GPS", "Distance to dest: " + here.distanceTo(dest));
*/
			 }}); 
		
	gombStop.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			stopService(myIntent);

		}
	});

	gombStart.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			startService(myIntent);
		}
	});
	
	/**	button.setOnClickListener(new OnClickListener() {

	@Override
	public void onClick(View v) {
		 double Lati = 55.43503;
		    double Long = 2.86869;
		    double destLat = 51.43503;
		    double destLng = 2.86869;
		
		final float[] results= new float[3];
		// Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), Double.valueOf(getLat), Double.valueOf(getLon), results);
		Location.distanceBetween(Lati, Long, destLat, destLng, results);
		
		Log.d("GPS", "results[0]: " + results[0]);
	    Log.d("GPS", "results[1]: " + results[1]);
	    Log.d("GPS", "results[2]: " + results[2]);
		textViewRes.setText(String.valueOf(results[2]));

	} 
});  */
	
/**	gombNetStop.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			stopService(NetLogIntent);

		}
	});

	gombNetStart.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
			startService(NetLogIntent);
		}
	});
*/

	}
	//logolás
	public void LogToFile (String szöveg){
		FileWriter fw;

		Calendar c = Calendar.getInstance();
		SimpleDateFormat fdate = new SimpleDateFormat("yyyy-MM-dd");
		final String formattedDate = fdate.format(c.getTime());

		SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
		final String formattedTime = ftime.format(c.getTime());

		File log = new File(Environment.getExternalStorageDirectory()
				+ "/Robot/App.log");
		
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory()
					+ "/Robot/App.log", true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("\r\n" + formattedDate + " " + formattedTime + " "
					+ szöveg);
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MediaScannerConnection.scanFile(DRMainActivity.this,
				new String[] { log.getAbsolutePath() }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {
					}

				});
	}
	//logolás vége
}

class doFileUploa extends AsyncTask<Void, Void, Void> {

@Override
protected Void doInBackground(Void... params) {
	FTPClient mFTP = new FTPClient();
    try {
        // Connect to FTP Server
        mFTP.connect("ftp.uw.hu");
        mFTP.login("readdeo", "5e2c94e0");
        mFTP.setFileType(FTP.BINARY_FILE_TYPE);
        mFTP.enterLocalPassiveMode();
        
        // Prepare file to be uploaded to FTP Server
        File file = new File(Environment.getExternalStorageDirectory() + "/Robot/PMLog.kml");
        FileInputStream ifile = new FileInputStream(file);
        
        // Upload file to FTP Server
        mFTP.storeFile("/uploads/PMLog.kml",ifile);
        mFTP.disconnect();          
    } catch (SocketException e) {
    	LogToFile ("DRMainActivity /Robot/PMLog.kml deltöltése sikertelen volt" +e);
    } catch (IOException e) {
    	LogToFile ("DRMainActivity /Robot/PMLog.kml deltöltése sikertelen volt" +e);
    }
	 
    return null;
}

protected void onPostExecute(Void result) {

    // Here if you wish to do future process for ex. move to another activity do here

}
//logolás
public void LogToFile (String szöveg){
	FileWriter fw;

	Calendar c = Calendar.getInstance();
	SimpleDateFormat fdate = new SimpleDateFormat("yyyy-MM-dd");
	final String formattedDate = fdate.format(c.getTime());

	SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
	final String formattedTime = ftime.format(c.getTime());

	File log = new File(Environment.getExternalStorageDirectory()
			+ "/Robot/App.log");
	
	try {
		fw = new FileWriter(Environment.getExternalStorageDirectory()
				+ "/Robot/App.log", true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.append("\r\n" + formattedDate + " " + formattedTime + " "
				+ szöveg);
		bw.close();
		fw.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
//logolás vége
}
class doFileUpload2 extends AsyncTask<Void, Void, Void> {

@Override
protected Void doInBackground(Void... params) {
	FTPClient mFTP = new FTPClient();
    try {
        // Connect to FTP Server
        mFTP.connect("ftp.uw.hu");
        mFTP.login("readdeo", "5e2c94e0");
        mFTP.setFileType(FTP.BINARY_FILE_TYPE);
        mFTP.enterLocalPassiveMode();
        
        // Prepare file to be uploaded to FTP Server
        File file = new File(Environment.getExternalStorageDirectory() + "/Robot/LineLog.kml");
        FileInputStream ifile = new FileInputStream(file);
        
        // Upload file to FTP Server
        mFTP.storeFile("/uploads/LineLog.kml",ifile);
        mFTP.disconnect();          
    } catch (SocketException e) {
    	LogToFile ("DRMainActivity /Robot/LineLog.kml deltöltése sikertelen volt" +e);
    } catch (IOException e) {
    	LogToFile ("DRMainActivity /Robot/LineLog.kml deltöltése sikertelen volt" +e);
    }
	 
    return null;
}

protected void onPostExecute(Void result) {

    // Here if you wish to do future process for ex. move to another activity do here

}
//logolás
public void LogToFile (String szöveg){
	FileWriter fw;

	Calendar c = Calendar.getInstance();
	SimpleDateFormat fdate = new SimpleDateFormat("yyyy-MM-dd");
	final String formattedDate = fdate.format(c.getTime());

	SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
	final String formattedTime = ftime.format(c.getTime());

	File log = new File(Environment.getExternalStorageDirectory()
			+ "/Robot/App.log");
	
	try {
		fw = new FileWriter(Environment.getExternalStorageDirectory()
				+ "/Robot/App.log", true);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.append("\r\n" + formattedDate + " " + formattedTime + " "
				+ szöveg);
		bw.close();
		fw.close();
	} catch (IOException e) {
		e.printStackTrace();
	}
}
//logolás vége
}