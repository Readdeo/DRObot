package com.example.gpslog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

public class DRServer extends Service {

	private ServerSocket serverSocket;

	Handler updateConversationHandler;

	Thread serverThread = null;

	public static final int SERVERPORT = 1598;

	@Override
	public IBinder onBind(Intent intent) {
		
		return null;
	}

	// ip
	public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			LogToFile ("DRServer getLocalIpAddress "+ex);
		}
		return null;
	}

	@Override
	public void onCreate() {
		Toast t = Toast.makeText(DRServer.this, "Server elindult",
				Toast.LENGTH_SHORT);
		t.show();
		LogToFile ("DRServer elindult");
		updateConversationHandler = new Handler();

		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();
	 FileWriter fw;

		// IP txt-be írása

		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory()
					+ "/Robot/IP.txt", false);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append(getLocalIpAddress());
			bw.close();
			fw.close();

			MediaScannerConnection.scanFile(DRServer.this,
					new String[] { Environment.getExternalStorageDirectory()
							+ "/Robot/IP.txt" }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						@Override
						public void onScanCompleted(String path, Uri uri) {
						}
					});
		} catch (IOException e) {
			LogToFile ("DRServer IP.txt írása sikertelen volt");
		}
		// IP írás vége
		new FileUpload().execute();
	}

	@Override
	public void onDestroy() {

		LogToFile ("DRServer leállt");
		try {
			serverSocket.close();
		} catch (IOException e) {
			LogToFile ("DRServer serversocket close "+e);
		}

		super.onDestroy();
	}

	class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				LogToFile ("DRServer serversocket "+e);
			}
			while (!Thread.currentThread().isInterrupted()) {

				try {

					socket = serverSocket.accept();

					CommunicationThread commThread = new CommunicationThread(
							socket);
					new Thread(commThread).start();

				} catch (IOException e) {
					LogToFile ("DRServer serversocket "+e);
				}
			}
		}
	}

	class CommunicationThread implements Runnable {

		private Socket clientSocket;

		private BufferedReader input;

		public CommunicationThread(Socket clientSocket) {

			this.clientSocket = clientSocket;

			try {

				this.input = new BufferedReader(new InputStreamReader(
						this.clientSocket.getInputStream()));

			} catch (IOException e) {
				LogToFile ("DRServer clientsocket "+e);
			}
		}

		public void run() {

			while (!Thread.currentThread().isInterrupted()) {

				try {

					String read = input.readLine();

					updateConversationHandler.post(new updateUIThread(read));

				} catch (IOException e) {
					LogToFile ("DRServer updateConversationHandler "+e);
				}
			}
		}

	}

	class updateUIThread implements Runnable {
		private String msg;

		public updateUIThread(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			if ("GPSstart".equals(msg)) {
				
				Intent i = new Intent();
				i.setClassName("com.example.gpslog",
						"com.example.gpslog.DataLogService");
				startService(i);
				LogToFile ("DataLogService indítási kísérlet");
			} else {

			}
			if ("GPSstop".equals(msg)) {
		
				Intent i = new Intent();
				i.setClassName("com.example.gpslog",
						"com.example.gpslog.DataLogService");
				stopService(i);
				LogToFile ("DataLogService leállítási kísérlet");
			} else {

			}
			if ("stClient".equals(msg)) {
				// startService(myIntent);
				Intent i = new Intent();
				i.setClassName("com.example.gpslog",
						"com.example.gpslog.DRClient");
				startService(i);
				LogToFile ("DRClient infítási kísérlet");
			} else {

			}
			if ("spClient".equals(msg)) {
				// startService(myIntent);
				Intent i = new Intent();
				i.setClassName("com.example.gpslog",
						"com.example.gpslog.DRClient");
				stopService(i);
				LogToFile ("DRClient leállítási kísérlet");
			} else {

			}

		}

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
class FileUpload extends AsyncTask<Void, Void, Void> {

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
        File file = new File(Environment.getExternalStorageDirectory() + "/Robot/IP.txt");
        FileInputStream ifile = new FileInputStream(file);
        
        // Upload file to FTP Server
        mFTP.storeFile("/uploads/IP.txt",ifile);
        mFTP.disconnect();          
    } catch (SocketException e) {
//TODO LOG
        e.printStackTrace();
    } catch (IOException e) {
//TODO LOG
        e.printStackTrace();
    }
	 
    return null;
}

protected void onPostExecute(Void result) {

    // Here if you wish to do future process for ex. move to another activity do here

}
}