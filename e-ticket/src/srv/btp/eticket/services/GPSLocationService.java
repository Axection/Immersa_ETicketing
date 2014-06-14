package srv.btp.eticket.services;

import java.util.Timer;
import java.util.TimerTask;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.Form_Main;
import srv.btp.eticket.R;
import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
	public LocationResult locationResult;
	public static final int SCAN_TIME = 180000; //OBSOLETE
	public static final int DISTANCE_LOCK = 0;
	public static Context baseContext = FormObjectTransfer.main_activity.getBaseContext();
	public Timer ctd;
	
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
		//ctd = new Timer();
		//ctd.schedule(new GetLastLocation(), SCAN_TIME);

	}
	
	public void StopGPS(){
		location_manager.removeUpdates(location_listener);
	}
	
	public boolean ActivateGPS() {
		location_flag = displayGpsStatus();
		location_listener = new MyLocationListener();
		if (location_flag) {
			Toast.makeText(baseContext, "GPS Menyala. Lakukan scanning...", Toast.LENGTH_SHORT).show();
			
			//Mengatur Demo Debug Mode
			FormObjectTransfer.main_activity.dbg_btnLeft.setVisibility(View.INVISIBLE);
			FormObjectTransfer.main_activity.dbg_btnRight.setVisibility(View.INVISIBLE);
			
			//END
			
			location_manager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, DISTANCE_LOCK, /*** telah diganti dari SCAN_TIME */
					location_listener);
			// GPS nyala, siapkan indikator :D
			FormObjectTransfer.isGPSConnected = true;
			RecreateTimer();
			
			FormObjectTransfer.main_activity.checkStatus();
			GPSIndicator.setImageResource(R.drawable.indicator_gps_warn);
			return true;
		} else {
			// GPS mati. siapkan indikator mati :(
			GPSIndicator.setImageResource(R.drawable.indicator_gps_off);
			FormObjectTransfer.main_activity.checkStatus();
			
			//mengetes mock
			if(!location_manager.isProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER)) {
				// Membuat test mock provider
				try{
	        	location_manager.addTestProvider(GPSLocationService.GPS_MOCK_PROVIDER, false, false,
	        			false, false, true, false, false, 0, 5);
	        	location_manager.setTestProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER, true);
	        	
	        	Log.e("ERROR GGENERATION","HAX");
	        	
				}
				catch(SecurityException e){
					//Perlu adanya exception untuk memberitahu bahwa GPS dan Mock tidak bisa dinyalakan. 
					FormObjectTransfer.main_activity.CallMockupError();
				}
			}  
	        
			//summon mock
	        if(location_manager.isProviderEnabled(GPSLocationService.GPS_MOCK_PROVIDER)) {
	        	GPSIndicator.setImageResource(R.drawable.indicator_gps_mocked);
	        	location_manager.requestLocationUpdates(GPSLocationService.GPS_MOCK_PROVIDER, 0, 0, location_listener);
	        	isMocked = true;
	        	FormObjectTransfer.isGPSConnected = true; //khusus mocking
	        	
	        	//dan beritahu bahwa program sedang dalam mode testing
	        	String msg = "";
	    		AlertDialog.Builder builder;
	            builder = new AlertDialog.Builder(FormObjectTransfer.main_activity);
	            msg = "Program terdeteksi sedang berjalan dalam mode testing dan SANGAT DILARANG digunakan dalam operasional. Apabila anda tidak mengetahui apa yang sedang terjadi, harap segera nyalakan GPS device dan restart aplikasi untuk keluar dari mode testing.";
	            builder.setTitle("PERINGATAN KERAS");
	    		builder.setMessage(msg);
	            builder.setCancelable(false);
	            builder.setNegativeButton("Lanjutkan Aplikasi", 
	                    new DialogInterface.OnClickListener() {
	        			// DO NOTHING
	                	@Override public void onClick(DialogInterface dialog, int id) {
	                	}
	                });
	            builder.setPositiveButton("Restart",
	            		new DialogInterface.OnClickListener() {
	    						@Override public void onClick(DialogInterface dialog, int which) {
	    							//Jalankan GPS Setting
	    							int REQUEST_ENABLE_GPS = 0;
	    							Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    							FormObjectTransfer.main_activity.startActivityForResult(enableGPSIntent, REQUEST_ENABLE_GPS);
	    								if (REQUEST_ENABLE_GPS == Activity.RESULT_OK) {
	    									//Restart~
	    				                	Intent mStartActivity = new Intent(FormObjectTransfer.main_activity.getBaseContext(), Form_Main.class);
	    									int mPendingIntentId = 45556;
	    									PendingIntent mPendingIntent = PendingIntent.getActivity(FormObjectTransfer.main_activity.getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
	    									AlarmManager mgr = (AlarmManager)FormObjectTransfer.main_activity.getBaseContext().getSystemService(Context.ALARM_SERVICE);
	    									mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400, mPendingIntent);
	    									System.exit(0);
	    								}else{
	    									//Restart~
	    				                	Intent mStartActivity = new Intent(FormObjectTransfer.main_activity.getBaseContext(), Form_Main.class);
	    									int mPendingIntentId = 45556;
	    									PendingIntent mPendingIntent = PendingIntent.getActivity(FormObjectTransfer.main_activity.getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
	    									AlarmManager mgr = (AlarmManager)FormObjectTransfer.main_activity.getBaseContext().getSystemService(Context.ALARM_SERVICE);
	    									mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400, mPendingIntent);
	    									System.exit(0);
	    								}
	    						}
	    					});
	            AlertDialog alert = builder.create();
	            try{
	            	alert.show();
	            }
	            catch(Exception e){
	            	
	            }
	            //end alert
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
			//ctd.cancel(); //OBSOLETE
			
			if(!isMocked){
				GPSIndicator.setImageResource(R.drawable.indicator_gps_on);
			}
			//Sekarang ini hanya untuk debugging
			Log.d("GPSLocationDebug",loc.getTime()+" timelock.");
			String txt = "Terdeteksi lokasi berpindah :\n Lat: " + loc.getLatitude() + " Lng: "
					+ loc.getLongitude();


			current_longitude = loc.getLongitude();
			current_latitude =  loc.getLatitude();
			FormObjectTransfer.main_activity.dbg_txtLog.setText(txt);
			
			PreferenceManager.getDefaultSharedPreferences(
					FormObjectTransfer.main_activity.getBaseContext())
					.edit()
						.putFloat("long", (float)current_longitude)
						.putFloat("lat", (float)current_latitude)
					.commit();
			
			//Dapatkan status pergerakan, apakah format maju atau format mundur
			String valueIntended = PreferenceManager.getDefaultSharedPreferences(
					FormObjectTransfer.main_activity.getBaseContext()).getString(
							"trajectory_direction", 
							FormObjectTransfer.main_activity.getResources().getStringArray(
									R.array.direction_entry)[0]);
			Log.d("VALUE_INTENDED",valueIntended);
			
			current_city = FormObjectTransfer.gdl.getNearestCity(current_latitude, current_longitude);
			Log.d("NEAREST_City",current_city + " is city " + FormObjectTransfer.gdl.kotaList[current_city-1]);
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
	            /**Masukkan data upload lokasi
	             * lokasi latitude diwakili oleh variabel "current_latitude"
	             * lokasi longitude diwakili oleh variabel "current_longitude"
	             * sebelumny ini mengambil data konfigurasi berapa jarak interval (dalam menit / *60 sec)
	             * aplikasi harus submit data.
	             */
			}
			//TODO :Submit lokasi ke raw_track_data via LocationSubmissionService.java
			//buat sebuah sistem timer untuk isReadyToSubmit selalu true setiap interval berapa detik.
			//Battle Begins! Mwahahahhahaha XD
			
			if(FormObjectTransfer.isReadyToSubmit){	
			LocationSubmissionService asyncTask = new LocationSubmissionService();
				//property load
				String id_bis = PreferenceManager.getDefaultSharedPreferences(FormObjectTransfer.main_activity.getApplicationContext())
						.getString("plat_bis", "-1");
				String execution[] = new String[]{
						id_bis,
						current_longitude+"",
						current_latitude+""
				};
				asyncTask.execute(execution);
				FormObjectTransfer.isReadyToSubmit = false;
			}
			RecreateTimer();
		}
		//Unused Callbacks
		@Override public void onProviderDisabled(String provider) {}
		@Override public void onProviderEnabled(String provider) {}
		@Override public void onStatusChanged(String provider, int status, Bundle extras) {}

	}
	
	//EXTENDED
	class GetLastLocation extends TimerTask {
        @Override
        public void run() {
             location_manager.removeUpdates(location_listener);
             Location gps_loc=null;
             try{
                 gps_loc=location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                 locationResult.gotLocation(gps_loc);
             }catch(NullPointerException e){
            	 RecreateTimer();
             }
             
        }
    }
	
	public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}


