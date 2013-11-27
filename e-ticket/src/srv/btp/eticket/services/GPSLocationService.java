package srv.btp.eticket.services;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.Form_Main;
import srv.btp.eticket.R;
import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
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
	public static final int SCAN_TIME = 60000;
	public static final int DISTANCE_LOCK = 50;
	public static Context baseContext = FormObjectTransfer.main_activity.getBaseContext();
	public CountDownTimer ctd;
	
	//Fast Data Move
	public double current_longitude = 0;
	public double current_latitude = 0;
	public int current_city = 0;
	public ImageView GPSIndicator;
	public int lastCity = 0;
	
	public static final String LOG_TAG = "GpsMockProvider";
	public static final String GPS_MOCK_PROVIDER = "GpsMockProvider";
	private boolean isMocked = false;
	public CountDownTimer cd;
	CRUD_Route_Table crud_forward = new CRUD_Route_Table(FormObjectTransfer.main_activity.getBaseContext());
	CRUD_Route_Back_Table crud_reverse = new CRUD_Route_Back_Table(FormObjectTransfer.main_activity.getBaseContext());
	
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
		RecreateTimer();
		
	}
	
	public void RecreateTimer(){
		ctd = new CountDownTimer(10000,1000){
			@Override
			public void onFinish() {
				if(!FormObjectTransfer.isQuit)
				ActivateGPS();
			}
			@Override public void onTick(long millisUntilFinished) {}
		};
	}
	public void StopGPS(){
		location_manager.removeUpdates(location_listener);
	}
	
	public boolean ActivateGPS() {
		location_flag = displayGpsStatus();
		location_listener = new MyLocationListener();
		if (location_flag) {
			Toast.makeText(baseContext, "GPS Menyala. Lakukan scanning...", Toast.LENGTH_SHORT).show();
			
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
			Toast.makeText(baseContext, "GPS Mati... menggunakan data DUMMY dari daftar koordinat.", Toast.LENGTH_SHORT).show();
			GPSIndicator.setImageResource(R.drawable.indicator_gps_off);
			FormObjectTransfer.main_activity.checkStatus();
			
			//mengetes mock
			if(!location_manager.isProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER)) {
	        	// Membuat test mock provider
	        	location_manager.addTestProvider(GPSLocationService.GPS_MOCK_PROVIDER, false, false,
	        			false, false, true, false, false, 0, 5);
	        	location_manager.setTestProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER, true);
	        	
			}  
	        
			//summon mock
	        if(location_manager.isProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER)) {
	        	GPSIndicator.setImageResource(R.drawable.indicator_gps_mocked);
	        	location_manager.requestLocationUpdates(GPSLocationService.GPS_MOCK_PROVIDER, 0, 0, location_listener);
	        	isMocked = true;
	        	FormObjectTransfer.isGPSConnected = true; //khusus mocking
	        }
			
			/*Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			FormObjectTransfer.main_activity.startActivity(viewIntent);
			ctd.cancel(); //Mematikan fungsi eksisting untuk menyiapkan overriding.
			RecreateTimer();
			ctd.start();*/
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
			if(!isMocked){
				GPSIndicator.setImageResource(R.drawable.indicator_gps_on);
			}
			//Sekarang ini hanya untuk debugging
			Log.d("GPSLocationDebug",loc.getTime()+" timelock.");
			Toast.makeText(
					baseContext,
					"Terdeteksi lokasi berpindah :\n Lat: " + loc.getLatitude() + " Lng: "
							+ loc.getLongitude(), Toast.LENGTH_SHORT).show();

			current_longitude = loc.getLongitude();
			current_latitude =  loc.getLatitude();
			
			//Dapatkan status pergerakan, apakah format maju atau format mundur
			String valueIntended = PreferenceManager.getDefaultSharedPreferences(
					FormObjectTransfer.main_activity.getBaseContext()).getString(
							"trajectory_direction", 
							FormObjectTransfer.main_activity.getResources().getStringArray(
									R.array.direction_entry)[0]);
			Log.d("VALUE_INTENDED",valueIntended);
			
				current_city = FormObjectTransfer.gdl.getNearestCity(current_latitude, current_longitude);
			
			if(current_city != lastCity){
				final Form_Main e = FormObjectTransfer.main_activity;
	            e.SetCityEnable(current_city);
	            e.CreateCityDisplay(e.city_display);

	            //ANIMASIKAN pergerakan
	            final int theconst = 40+ (179 * (e.city_real_position-1));
	            final int getX = e.top_scroll.getScrollX();
	            cd = new CountDownTimer(600, 10) {
	            	@Override
	            	public void onTick(long millisUntilFinished) {
	            		e.top_scroll.scrollTo((int)(theconst + ((getX-theconst) * millisUntilFinished/600)),0);
	            	}
	            	@Override
	            	public void onFinish() {
	            		e.top_scroll.scrollTo(theconst,0);
	            	}
	            };
	            cd.start();
	            //Animasi selesai
	            lastCity = current_city;
			}

			/*----------to get City-Name from coordinates 
			 * 			Opsional ------------- */
			Geocoder gcd = new Geocoder(baseContext, Locale.getDefault());
			List<Address> addresses;
			try {
				addresses = gcd.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
				if (addresses.size() > 0)
					Log.d(this.getClass().toString(),addresses.get(0).getLocality());
				//current_city = addresses.get(0).getLocality();
			} catch (IOException ee) {
				ee.printStackTrace();
			}

		}
		//Unused Callbacks
		@Override public void onProviderDisabled(String provider) {}
		@Override public void onProviderEnabled(String provider) {}
		@Override public void onStatusChanged(String provider, int status, Bundle extras) {}

	}
}


