package srv.btp.eticket.services;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

public class GPSLocationService {
	/***
	 * GPSLocationService.java
	 * 
	 * Kelas ini dibuat untuk mendapatkan lokasi tablet via GPS/a-GPS
	 * serta translasi lokasi GPS menjadi nama kota (LBS)
	 * Fungsi yang dibutuhkan : 
	 * - Fungsi dapatin lokasi device
	 * - Fungsi menyocokkan lokasi dengan daftar kota
	 *
	 * 
	 * Info menyusul
	 */
	public boolean location_flag = false;
	public MyLocationListener location_listener;
	public LocationManager location_manager;
	public static final int SCAN_TIME = 5000;
	public static final int DISTANCE_LOCK = 50;
	public static Context baseContext = FormObjectTransfer.main_activity.getBaseContext();
	private CountDownTimer ctd;
	
	//Fast Data Move
	public double current_longitude = 0;
	public double current_latitude = 0;
	public String current_city = "";
	public ImageView GPSIndicator;
	
	private Boolean displayGpsStatus() {
		ContentResolver contentResolver = baseContext.getContentResolver();
		boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(
				contentResolver, LocationManager.GPS_PROVIDER);
		if (gpsStatus) {
			return true;

		} else {
			return false;
		}
	}

	public GPSLocationService(ImageView indicator){
		super();
		GPSIndicator = indicator;
		location_manager = (LocationManager) baseContext.getSystemService(Context.LOCATION_SERVICE);
		ctd = new CountDownTimer(10000,1000){
			@Override
			public void onFinish() {
				if(!FormObjectTransfer.isQuit)
				ActivateGPS();
			}
			@Override public void onTick(long millisUntilFinished) {}
		};
	}
	
	public boolean ActivateGPS() {
		location_flag = displayGpsStatus();
		if (location_flag) {
			Toast.makeText(baseContext, "GPS Menyala. Lakukan scanning...", Toast.LENGTH_SHORT).show();
			location_listener = new MyLocationListener();
			location_manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, SCAN_TIME, DISTANCE_LOCK,
					location_listener);
			// GPS nyala, siapkan indikator :D
			FormObjectTransfer.isGPSConnected = true;
			//if(!FormObjectTransfer.isInitalizationState)
			FormObjectTransfer.main_activity.checkStatus();
			GPSIndicator.setImageResource(R.drawable.indicator_gps_warn);
			return true;
		} else {
			// GPS mati. siapkan indikator mati :(
			Toast.makeText(baseContext, "GPS Mati... harap hidupkan secara manual...", Toast.LENGTH_SHORT).show();
			GPSIndicator.setImageResource(R.drawable.indicator_gps_off);
			FormObjectTransfer.main_activity.checkStatus();
			Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			FormObjectTransfer.main_activity.startActivity(viewIntent);
			ctd.cancel(); //Mematikan fungsi eksisting untuk menyiapkan overriding.
			ctd.start();
			return false;
		}
	}

	private class MyLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location loc) {
			/**
			 * Setiap posisinya berpindah, listener ini akan terus terupdate.
			 * Mendapatkan lokasi baru dan dicocokkan dengan daftar kota.
			 */
			GPSIndicator.setImageResource(R.drawable.indicator_gps_on);
			Toast.makeText(
					baseContext,
					"Terdeteksi lokasi berpindah :\n Lat: " + loc.getLatitude() + " Lng: "
							+ loc.getLongitude(), Toast.LENGTH_SHORT).show();

			current_longitude = loc.getLongitude();
			current_latitude =  loc.getLatitude();

			/*----------to get City-Name from coordinates ------------- */
			Geocoder gcd = new Geocoder(baseContext, Locale.getDefault());
			List<Address> addresses;
			try {
				addresses = gcd.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
				if (addresses.size() > 0)
					System.out.println(addresses.get(0).getLocality());
				current_city = addresses.get(0).getLocality();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		//Unused Callbacks
		@Override public void onProviderDisabled(String provider) {}
		@Override public void onProviderEnabled(String provider) {}
		@Override public void onStatusChanged(String provider, int status, Bundle extras) {}

	}
}


