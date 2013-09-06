package com.example.gpslog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.R.string;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DRClient extends Service {

	// private ServerSocket serverSocket;

	Handler updateConversationHandler;

	Thread serverThread = null;

	private Socket socket;
	string stringText;
	private static final int SERVERPORT = 1599;
	LocationListener mListener;
	LocationManager lm;

	public static boolean available(int port) {
		port = SERVERPORT;
		ServerSocket ss = null;
		DatagramSocket ds = null;
		try {
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		} catch (IOException e) {
		} finally {
			if (ds != null) {
				ds.close();
			}

			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					/* should not be thrown */
				}
			}
		}

		return false;
	}

	// Saját IP olvasás és feltöltés

	// ip
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			LogToFile("DRClient getLocalIpAddress" + ex);
		}
		return null;
	}

	// Saját IP vége
	@Override
	public void onCreate() {
		super.onCreate();
		LogToFile("DRClient elindult");
		new Thread(new ClientThread()).start();
		Toast t = Toast.makeText(DRClient.this, "Client elindult",
				Toast.LENGTH_LONG);
		t.show();
		// IP txt-be írása
		FileWriter fw;
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory()
					+ "/Robot/IP.txt", false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append(getLocalIpAddress());
			bw.close();
			fw.close();

			MediaScannerConnection.scanFile(DRClient.this,
					new String[] { Environment.getExternalStorageDirectory()
							+ "/Robot/IP.txt" }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						@Override
						public void onScanCompleted(String path, Uri uri) {
						}
					});
		} catch (IOException e) {
			LogToFile("DRClient IP.txt írása sikertelen volt");
		}
		new doFileUpload().execute();

		// IP írás vége
		// IP2 letöltés
		new doFileDownload().execute();

		new Thread(new ClientThread()).start();

		Calendar c = Calendar.getInstance();
		SimpleDateFormat fdate = new SimpleDateFormat("yyyy-MM-dd");
		final String formattedDate = fdate.format(c.getTime());

		SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
		final String formattedTime = ftime.format(c.getTime());

		// Get the text file
		File fileT = new File(Environment.getExternalStorageDirectory()
				+ "/Robot/Config/GPSListenerminTime.cfg");

		// Read text from file
		StringBuilder GPSListenerminTime = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileT));
			String line;

			while ((line = br.readLine()) != null) {
				GPSListenerminTime.append(line);
				GPSListenerminTime.append('\n');

			}
		} catch (IOException e) {
			LogToFile("DRClient /Robot/Config/GPSListenerminTime.cfg olvasása sikertelen volt");
		}

		// GPS jelek olvasása
		lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
		mListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location loc) {
				try {

					PrintWriter out = new PrintWriter(new BufferedWriter(
							new OutputStreamWriter(socket.getOutputStream())),
							true);
					out.println(loc.getLatitude() + ";" + loc.getLongitude());
				} catch (UnknownHostException e) {
					LogToFile("DRClient socket " + e);

				} catch (IOException e) {
					LogToFile("DRClient socket " + e);

				} catch (Exception e) {
					LogToFile("DRClient socket " + e);

				}

			}

			@Override
			public void onProviderDisabled(String provider) {

			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {

			}

		};
		// lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// Long.valueOf(GPSListenerminTime.toString().trim()), 0,
		// mListener);
		// TODO csere
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 0, mListener);
	}

	class ClientThread implements Runnable {

		@Override
		public void run() {
			// Get the text file
			File fileIP2 = new File(Environment.getExternalStorageDirectory()
					+ "/Robot/IP2.txt");

			// Read text from file
			StringBuilder IP2 = new StringBuilder();
			try {
				BufferedReader br = new BufferedReader(new FileReader(fileIP2));
				String line;

				while ((line = br.readLine()) != null) {
					IP2.append(line);
					IP2.append('\n');
				}
			} catch (IOException e) {

			}
			try {
				InetAddress serverAddr = InetAddress.getByName(IP2.toString()
						.trim());

				socket = new Socket(serverAddr, SERVERPORT);

			} catch (UnknownHostException e1) {
				e1.printStackTrace();

			} catch (IOException e1) {
				e1.printStackTrace();

			}

		}

	}

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}

	@Override
	public void onDestroy() {
		lm.removeUpdates(mListener);
		LogToFile("DRClient leállt");
		super.onDestroy();
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
			MediaScannerConnection.scanFile(DRClient.this,
					new String[] { log.getAbsolutePath() }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						@Override
						public void onScanCompleted(String path, Uri uri) {
						}

					});
		}
		//logolás vége   LogToFile("");
}

class doFileUpload extends AsyncTask<Void, Void, Void> {

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
			File file = new File(Environment.getExternalStorageDirectory()
					+ "/Robot/IP.txt");
			FileInputStream ifile = new FileInputStream(file);

			// Upload file to FTP Server
			mFTP.storeFile("/uploads/IP.txt", ifile);
			mFTP.disconnect();
		} catch (SocketException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return null;
	}

	protected void onPostExecute(Void result) {

		// Here if you wish to do future process for ex. move to another
		// activity do here

	}
}

class doFileDownload extends AsyncTask<Void, Void, Void> {

	@Override
	protected Void doInBackground(Void... params) {
		FTPClient con = null;

		try {
			con = new FTPClient();
			con.connect("ftp.uw.hu");

			if (con.login("readdeo", "5e2c94e0")) {
				con.enterLocalPassiveMode(); // important!
				con.setFileType(FTP.BINARY_FILE_TYPE);
				String data = Environment.getExternalStorageDirectory()
						+ "/Robot/IP2.txt";

				OutputStream out = new FileOutputStream(new File(data));
				boolean result = con.retrieveFile("/uploads/IP2.txt", out);
				out.close();
				if (result)
					Log.v("download result", "succeeded");
				con.logout();
				con.disconnect();
			}
		} catch (Exception e) {
			Log.v("download result", "failed");
			e.printStackTrace();
		}

		return null;
	}

	protected void onPostExecute(Void result) {

		// Here if you wish to do future process for ex. move to another
		// activity do here

	}
}