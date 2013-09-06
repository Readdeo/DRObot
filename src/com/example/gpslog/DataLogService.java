package com.example.gpslog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

public class DataLogService extends Service {

	@Override
	public IBinder onBind(Intent intent) {

		return null;
	}
	LocationManager lm;
		LocationListener mListener;
	@Override
	public void onCreate() {

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
					LogToFile("/Robot/Config/GPSListenerminTime.cfg olvasása sikertelen volt");
				}	

		
				LogToFile("GPSLogService elindult");

		Toast.makeText(getBaseContext(), "Service elindult", Toast.LENGTH_LONG)
				.show();
		
		
		lm = (LocationManager)this.getSystemService(LOCATION_SERVICE);
		mListener = new LocationListener() {
		    public void onLocationChanged(Location loc) {
		    	// célpont távolságának és irányának számítása
				double currentLat = loc.getLatitude();
			    double currentLng = loc.getLongitude();

			    double destLat = 46.860;
			    double destLng = 17.450;

			    final float[] results= new float[3];
			    Location.distanceBetween(currentLat, currentLng, destLat, destLng, results);

				Calendar c = Calendar.getInstance();
				SimpleDateFormat fdate = new SimpleDateFormat("yyyy-MM-dd");
				final String formattedDate = fdate.format(c.getTime());

				SimpleDateFormat ftime = new SimpleDateFormat("HH:mm:ss");
				final String formattedTime = ftime.format(c.getTime());

				// Vonal KML írása
				File inputFile = new File(Environment.getExternalStorageDirectory()
						+ "/Robot/LineLog.kml");
				File tempFile = new File(Environment.getExternalStorageDirectory()
						+ "/Robot/LineLogTMP.kml");
				File PMinputFile = new File(Environment.getExternalStorageDirectory()
						+ "/Robot/PMLog.kml");
				File PMtempFile = new File(Environment.getExternalStorageDirectory()
						+ "/Robot/PMLogTMP.kml");

				FileWriter fw;
				try {
					fw = new FileWriter(tempFile, true);

					BufferedWriter bw = new BufferedWriter(fw);
					BufferedReader reader = new BufferedReader(
							new FileReader(inputFile));

					String lineToRemove = "</coordinates> </LineString> </MultiGeometry> </Placemark> </kml>";
					String currentLine;

					while ((currentLine = reader.readLine()) != null) {
						String trimmedLine = currentLine.trim();
						if (trimmedLine.equals(lineToRemove))
							continue;
						bw.write(currentLine);

					}
					reader.close();
					bw.append(String.valueOf(loc.getLongitude()
							+ ","
							+ String.valueOf(loc.getLatitude())
							+ ",0 "
							+ "\r\n"
							+ "</coordinates> </LineString> </MultiGeometry> </Placemark> </Folder> </Document> </kml>"));
					bw.close();
					fw.close();
					boolean successful = tempFile.renameTo(inputFile);

					// bw.append(("asdasdasdasdasd asdasdasdasdasd," + "\r\n" +
					// "</coordinates> </LineString> </MultiGeometry> </Placemark> </Folder> </Document> </kml>"));

					/**
					 * // DataLog.csv írása fw = new
					 * FileWriter(Environment.getExternalStorageDirectory() +
					 * "/DataLog.csv", true);
					 * 
					 * bw.append(formattedDate + ";" + formattedTime + ";" +
					 * (String.valueOf(loc.getLatitude()) + ";" +
					 * String.valueOf(loc.getLongitude()) + ";" +
					 * String.valueOf(loc.getSpeed()) + ";" +
					 * String.valueOf(loc.getAltitude()) + ";" + String
					 * .valueOf(loc.getBearing() + "\r\n"))); bw.close(); fw.close();
					 */
					File csv = new File(Environment.getExternalStorageDirectory(),
							"/Robot/LineLog.kml");

					MediaScannerConnection.scanFile(DataLogService.this,
							new String[] { csv.getAbsolutePath() }, null,
							new MediaScannerConnection.OnScanCompletedListener() {
								@Override
								public void onScanCompleted(String path, Uri uri) {
								}

							});
				} catch (IOException e) {
					e.printStackTrace();
					LogToFile("LineLog.kml írása sikertelen volt.");

				}

				// Placemark KML írása

				try {
					fw = new FileWriter(PMtempFile, true);

					BufferedWriter bw = new BufferedWriter(fw);
					BufferedReader reader = new BufferedReader(new FileReader(
							PMinputFile));

					String PMlineToRemove = "</Document></kml>";
					String currentLine;

					while ((currentLine = reader.readLine()) != null) {
						String trimmedLine = currentLine.trim();
						if (trimmedLine.equals(PMlineToRemove))
							continue;
						bw.write(currentLine);

					}
					reader.close();
					bw.append("<Placemark><name>Trip</name><description>"
							+ fdate
							+ "\r\n"
							+ ftime
							+ "\r\n"
							+ "Sebesség:"
							+ loc.getSpeed()
							+ "\r\n"
							+ "GPS Irány:"
							+ loc.getBearing()
							+ "\r\n"
							+ "Magasság:"
							+ loc.getAltitude()
							+ "\r\n"
							+ "Pontosság:"
							+ loc.getAccuracy()
							+ "</description><Point><coordinates>"
							+ String.valueOf(loc.getLongitude()
									+ ","
									+ String.valueOf(loc.getLatitude()
											+ "</coordinates></Point></Placemark>"
											+ "\r\n" + "</Document></kml>")));
					bw.close();
					fw.close();
					boolean successful = PMtempFile.renameTo(PMinputFile);

					// bw.append(("asdasdasdasdasd asdasdasdasdasd," + "\r\n" +
					// "</coordinates> </LineString> </MultiGeometry> </Placemark> </Folder> </Document> </kml>"));

					/**
					 * // DataLog.csv írása fw = new
					 * FileWriter(Environment.getExternalStorageDirectory() +
					 * "/DataLog.csv", true);
					 * 
					 * bw.append(formattedDate + ";" + formattedTime + ";" +
					 * (String.valueOf(loc.getLatitude()) + ";" +
					 * String.valueOf(loc.getLongitude()) + ";" +
					 * String.valueOf(loc.getSpeed()) + ";" +
					 * String.valueOf(loc.getAltitude()) + ";" + String
					 * .valueOf(loc.getBearing() + "\r\n"))); bw.close(); fw.close();
					 */
					File csv = new File(Environment.getExternalStorageDirectory(),
							"/Robot/PMLog.kml");

					MediaScannerConnection.scanFile(DataLogService.this,
							new String[] { csv.getAbsolutePath() }, null,
							new MediaScannerConnection.OnScanCompletedListener() {
								@Override
								public void onScanCompleted(String path, Uri uri) {
								}

							});
				} catch (IOException e) {
					e.printStackTrace();
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

		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				Long.valueOf(GPSListenerminTime.toString().trim()), 0, mListener);
		
		super.onCreate();

	}

	@Override
	public void onDestroy() {
		lm.removeUpdates(mListener);
		LogToFile("GPSLogService Leállt");
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
		MediaScannerConnection.scanFile(DataLogService.this,
				new String[] { log.getAbsolutePath() }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					@Override
					public void onScanCompleted(String path, Uri uri) {
					}

				});
	}
	//logolás vége   LogToFile("");
}
