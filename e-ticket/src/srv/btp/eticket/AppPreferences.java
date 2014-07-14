package srv.btp.eticket;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.services.BusIdentifierService;
import srv.btp.eticket.services.LoginService;
import srv.btp.eticket.services.RouteService;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class AppPreferences extends PreferenceActivity {
	/**
	 * Determines whether to always show the simplified settings UI, where
	 * settings are presented in a single list. When false, settings are shown
	 * as a master/detail two-pane view on tablets. When true, a single pane is
	 * shown on tablets.
	 */
	private static final boolean ALWAYS_SIMPLE_PREFS = true;
	
	private CharSequence[] btListAddress;
	private CharSequence[] btListNames;
	private ListPreference bluetoothList;

	private ListPreference route_list;
	private ListPreference plat_bis = (ListPreference) findPreference("plat_bis");
	private CharSequence[] routeListCode;
	private CharSequence[] routeListName;
	
	private BluetoothPrintService btx;

	private RouteService rd;
	private LoginService logins;
	
	protected Timer timerLogin = new Timer(true);

	
	protected static boolean isRouteClicked = false;
	protected static boolean isRestart = false;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Must Have
		super.onCreate(savedInstanceState);
        FormObjectTransfer.current_activity = this;
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		plat_bis = (ListPreference) findPreference("plat_bis");
		setupSimplePreferencesScreen();

		// pemeriksaan asal dari intent
		try {
			if (this.getIntent().getBooleanExtra("fromParent", false)) {
				findPreference("pref_quit").setEnabled(true);
			} else {
				/*
				 * findPreference("pref_quit").setEnabled(false);
				 * findPreference("pref_quit").setSelectable(false);
				 * findPreference("pref_quit").setTitle("");
				 * findPreference("pref_quit").setSummary("");
				 */

			}
		} catch (NullPointerException e) {
			findPreference("pref_quit").setEnabled(false);
			findPreference("pref_quit").setSelectable(false);
			findPreference("pref_quit").setTitle("");
			findPreference("pref_quit").setSummary("");
		}
		//Cabut paksa tombol quit.
		getPreferenceScreen().removePreference(findPreference("pref_quit"));
		//registrasi plat_bis
		
		//registrasi klik Route
		findPreference("route_list").setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				isRouteClicked = true;
				return false;
			}
		});
		
		// Preference for Bluetooth List
		bluetoothList = (ListPreference) findPreference("bluetooth_list");
		btx = new BluetoothPrintService(this, null);
		if (btx.FindPrinters() == 0) {
			Log.d("BTX", btx.toString() + " address access : "
					+ btx.getBtAddr().size());
			btListAddress = new CharSequence[btx.getBtAddr().size()];
			btListNames = new CharSequence[btx.getBtAddr().size()];
			Log.d("BTX", btListAddress.toString() + " " + btListNames
					+ " access address");
			int a = 0;
			for (String s : btx.getBtAddr()) {
				// Log.d("BTX",s + " get address");
				btListNames[a] = s.substring(0, s.indexOf("|"));
				btListAddress[a] = s.substring(s.indexOf("|") + 1, s.length());
				Log.d("BTXData", btListAddress[a] + " ... " + btListNames[a]);
				a++;
			}
			bluetoothList.setEntries(btListNames);
			bluetoothList.setEntryValues(btListAddress);
			String entity = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("bluetooth_list", "-1");
			List<CharSequence> btNames = Arrays.asList(btListAddress);
			if(!entity.equals("-1")){
				bluetoothList.setSummary(btListNames[btNames.indexOf(entity)]);
			}
			else{
				bluetoothList.setSummary("Wajib : Silahkan pilih bluetooth printer yang sudah ter-pairing.");
			}
			Log.d("BTX", bluetoothList.toString() + " post address");
			bluetoothList
					.setOnPreferenceClickListener(new OnPreferenceClickListener() {
						@Override
						public boolean onPreferenceClick(Preference arg0) {
							// debug
							for (int aa = 0; aa < btListNames.length; aa++) {
								Log.d("PrefBTXData",
										bluetoothList.getEntries()[aa]
												+ " = "
												+ bluetoothList
														.getEntryValues()[aa]);
							}
							bluetoothList.setEntries(btListNames);
							bluetoothList.setEntryValues(btListAddress);
							return false;
						}
					});

		} else {
			bluetoothList.setEnabled(false);
		}
		
		// setting nilai summary-summary
		findPreference("input_password").setSummary("****");
		timerLogin.schedule(LoginUpdate, Calendar.getInstance().getTime(), 1000);
		CallPassword(0);
		
	}
	protected TimerTask TaskUpdate = new TimerTask() {
		@Override
		public void run() {
			route_list = (ListPreference) findPreference("route_list");
			if(RouteService.isDone){
				this.cancel();
				FormObjectTransfer.current_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(RouteService.isFail){
							Log.e("RouteService","Fail to set route");
							Toast.makeText(FormObjectTransfer.current_activity.getBaseContext(), "Ada masalah pada jaringan. Rute untuk sementara tidak dapat diganti", Toast.LENGTH_LONG).show();
							route_list.setEnabled(false);
						}else if(RouteService.isDone){
							route_list.setEnabled(true);
							route_list.setEntries(FormObjectTransfer.routeName);
							route_list.setEntryValues(FormObjectTransfer.routeID);
							
							String entity = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("route_list", "-1");
							List<CharSequence> lists = Arrays.asList(FormObjectTransfer.routeID);
							if(!entity.equals("-1")){
								try{
									route_list.setSummary(FormObjectTransfer.routeName[lists.indexOf(entity)]);
								}
								catch(ArrayIndexOutOfBoundsException ee){
									ee.printStackTrace();
									route_list.setEnabled(false);
									route_list.setSummary("Tidak dapat mengubah daftar rute disebabkan error.");
								}
								
							}
							else{
								route_list.setSummary("Wajib : Silahkan pilih salah satu dari daftar rute berikut");
							}
						}

					}
				});
			}
		}
	};

	TimerTask Task = new TimerTask() {
		@Override
		public void run() {
			plat_bis = (ListPreference) findPreference("plat_bis");
			if(BusIdentifierService.isDone){
				this.cancel();
				FormObjectTransfer.current_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if(BusIdentifierService.isFail){
							Toast.makeText(FormObjectTransfer.current_activity.getBaseContext(), "Ada masalah pada jaringan. Tidak dapat mengganti plat bus.", Toast.LENGTH_LONG).show();
							plat_bis.setEnabled(false);
						}else if(BusIdentifierService.isDone){
							plat_bis.setEnabled(true);
							try{
								plat_bis.setEntries(bus.getCharSequenceFromArray(bus.FIELD_PLAT_NO));
								plat_bis.setEntryValues(bus.getCharSequenceFromArray(bus.FIELD_ID));
								plat_bis.setSummary(bus.getCharSequenceFromArray(bus.FIELD_PLAT_NO)[Integer.parseInt(
								                                                                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("plat_bis", "0")
								                                                                    )-1] );
							PreferenceManager.getDefaultSharedPreferences(
									plat_bis.getContext())
									.edit()
									.putString("plat_bis_hidden", plat_bis.getSummary()+"" )
									.commit();
							}
							catch(ArrayIndexOutOfBoundsException ae){
								Log.d("BusIdentifier","First Run Detected,manually readjust.");
								plat_bis.setSummary("Wajib : Silahkan pilih daftar plat bis yang sesuai");
								plat_bis.setEnabled(true);
							}
							catch(Exception e){
								e.printStackTrace();
								plat_bis.setEnabled(false);
								route_list.setEnabled(false);
							}
						}

					}
				});
			}
		}
	};

	protected BusIdentifierService bus;


	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		timerLogin.cancel();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		//LEMPAR SEMUA VALUE! >_<
	}
	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		if (!isSimplePreferences(this)) {
			return;
		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Bind the summaries of EditText/List/Dialog/Ringtone preferences to
		// their values. When their values change, their summaries are updated
		// to reflect the new value, per the Android Design guidelines.
		bindPreferenceSummaryToValue(findPreference("plat_bis"));
		bindPreferenceSummaryToValue(findPreference("unique_key"));
		bindPreferenceSummaryToValue(findPreference("bluetooth_list"));
		bindPreferenceSummaryToValue(findPreference("input_password"));
		bindPreferenceSummaryToValue(findPreference("route_list"));
		bindPreferenceSummaryToValue(findPreference("trajectory_direction"));
		//experimental
		bindPreferenceSummaryToValue(findPreference("service_address"));
		
		//for trajectory
		Preference asp = findPreference("trajectory_direction");
		String daKey = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("trajectory_direction", "0");
		Log.e("Trajectory",daKey);
		if(daKey.equals("0"))
			asp.setSummary("Wajib : Silahkan pilih arah trayek");
		//Tambahan fitur keluar
		Preference p = findPreference("pref_quit");
		p.setSummary(
	                 PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("interval", "1") //dalam menit
	                 + " menit"
				);
		p.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				CallExit();
				return false;
			}
		});
		Preference pp = findPreference("pref_about");
		pp.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				CallAbout();
				return false;
			}
		});
		
		Preference prefRestore = findPreference("pref_restore");
		prefRestore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				CallRestoreDefaults();
				return false;
			}
		});


		
		Preference prefRestart = findPreference("pref_restart");
		prefRestart.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				isRestart = true;
				CallExit();
				return false;
			}
		});
	}



//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
//HARDCODED
//////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////
	
//!region hardcoded
	/** {@inheritDoc} */
	@Override
	public boolean onIsMultiPane() {
		return isXLargeTablet(this) && !isSimplePreferences(this);
	}

	/**
	 * Helper method to determine if the device has an extra-large screen. For
	 * example, 10" tablets are extra-large.
	 */
	private static boolean isXLargeTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	/**
	 * Determines whether the simplified settings UI should be shown. This is
	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	 * doesn't have an extra-large screen. In these cases, a single-pane
	 * "simplified" settings UI should be shown.
	 */
	private static boolean isSimplePreferences(Context context) {
		return ALWAYS_SIMPLE_PREFS;
	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object value) {
			String stringValue = value.toString();

			if (preference instanceof ListPreference) {
				// For list preferences, look up the correct display value in
				// the preference's 'entries' list.
				ListPreference listPreference = (ListPreference) preference;
				//if(!preference.getKey().equals("plat_bis")){
				int index = -1;
				//if(stringValue != null){
					index = listPreference.findIndexOfValue(stringValue);
				//}
					// Set the summary to reflect the new value.
					preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
				//}else{
					
				//}
				if(preference.getKey().equals("route_list") && isRouteClicked == true){
					Log.e("UNIQUE_FORCE_CHANGE","CHANGE to 0");
					PreferenceManager.getDefaultSharedPreferences(
							FormObjectTransfer.current_activity.getApplicationContext())
							.edit()
							.putString("unique_key", "0")
							.commit();
					
					CRUD_Route_Table e = new CRUD_Route_Table(FormObjectTransfer.current_activity.getApplicationContext());
					e.onUpgrade(e.getWritableDatabase(), 1, 1);
					CRUD_Route_Back_Table ed = new CRUD_Route_Back_Table(FormObjectTransfer.current_activity.getApplicationContext());
					ed.onUpgrade(e.getWritableDatabase(), 1, 1);
					
					String txt = "Harap muat ulang (restart) program menggunakan menu 'Restart' untuk menerima efek perubahan.";
					Toast.makeText(FormObjectTransfer.current_activity.getApplicationContext(), txt, Toast.LENGTH_LONG).show();
				
					isRouteClicked = false;	
				}
				
				if(preference.getKey().equals("plat_bis")){
					String summary=PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString("plat_bis_hidden", "");
					Log.d("Platbis","Changed from " + summary + " to " + preference.getSummary());
							PreferenceManager.getDefaultSharedPreferences(
								preference.getContext())
								.edit()
								.putString("plat_bis_hidden", preference.getSummary()+"" )
								.commit();
					}
				if(preference.getKey().equals("input_password")){
					preference.setSummary("****");
				}
				
			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
			}
			
			
			if(isRouteClicked){
				}
			return true;
		}
	};

	/**
	 * Binds a preference's summary to its value. More specifically, when the
	 * preference's value is changed, its summary (line of text below the
	 * preference title) is updated to reflect the value. The summary is also
	 * immediately updated upon calling this method. The exact display format is
	 * dependent on the type of preference.
	 * 
	 * @see #sBindPreferenceSummaryToValueListener
	 */
	private static void bindPreferenceSummaryToValue(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
		
		// Trigger the listener immediately with the preference's
		// current value.
		sBindPreferenceSummaryToValueListener.onPreferenceChange(
				preference,
				PreferenceManager.getDefaultSharedPreferences(
						preference.getContext()).getString(preference.getKey(),
						""));
	}
//!endregion

	public void CallPassword(int ErrorCode) {
		final String thePassword =  PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("input_password","1234");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Login");
		switch (ErrorCode){
		case 0:
			builder.setMessage("Silahkan login terlebih dahulu untuk mengatur konfigurasi sistem.");
			break;
		case -1:
			builder.setMessage("Login gagal, username & password tidak ditemukan atau salah.");
			break;
		case -1024:
			builder.setMessage("Login gagal, periksa kembali koneksi.");
		default:
			builder.setMessage("Login gagal, terjadi galat. Cobalah beberapa saat lagi.");
			break;
		}
		
		
		
		final Activity thisAct = this;
		LayoutInflater inflater = thisAct.getLayoutInflater();
		View theContent = inflater.inflate(R.layout.sign, null);
		builder.setView(theContent);
		builder.setCancelable(false);
		builder.setPositiveButton("Masuk", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Dialog dlg = (Dialog) arg0;
				EditText usname = (EditText) dlg.findViewById(R.id.txtUsername);
				EditText passwd = (EditText) dlg.findViewById(R.id.txtpassword);

				arg0.dismiss();
				///TODO: Buat sistem login baru
				logins = new LoginService();
				//get the username password data
				
				String DATA_LOGIN[] = {usname.getText() + "", passwd.getText()+ ""};
				Log.d("LoginService","initializing Login with data : " + usname.getText() + " & " + usname.getText());

				try {
					logins.execute(DATA_LOGIN);
				} catch (Exception e) {
					CallPassword(-8);
				}
				
			}
		}
		);
		builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di, int wat) {
				di.cancel();
				thisAct.onBackPressed();
			}
		});

		AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(false);
		alert.show();
	}
	
	//CallPassword Extension
	protected TimerTask LoginUpdate = new TimerTask() {
		@Override
		public void run() {
			if(LoginService.isDone){
				//LoginUpdate.cancel();
				LoginService.isDone = false;
				FormObjectTransfer.current_activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Log.e("LoginUpdate","Read status LoginService isFail = " + LoginService.isFail);
						if(LoginService.isFail){
							LoginService.isFail = false;
							CallPassword(FormObjectTransfer.UserID);	
						}else{
							//set preferensi
							timerLogin.cancel();
							Preference p = findPreference("username");
							p.setSummary(FormObjectTransfer.UserName);
							//TODO : ini perlu di cross check lagi agar permintaan rute bis 
							// dan rute berdasarkan user_id yang telah di set.
							//Ambil Bis~
							bus  = new BusIdentifierService();
							String[] execution = {BusIdentifierService.URL_SERVICE_BUS + FormObjectTransfer.UserID}; //TODO URL ketambahan FormObject
							Log.d("BusService","Working...");
							Timer te = new Timer(true);
							te.schedule(Task, Calendar.getInstance().getTime(), 1000);
							try {
								bus.execute(execution);
							} catch (Exception e) {
								te.cancel();
								plat_bis.setEnabled(false);
								route_list.setEnabled(false);
							}
							// Preference for Route List
							route_list = (ListPreference) findPreference("route_list");
							// Ambil Rute
							rd = new RouteService();
							String URL_LIST_SERVICE[] = { RouteService.URL_SERVICE_TRAJECTORY };
							Log.d("RouteService","Working...");
							Timer td = new Timer(true);
							td.schedule(TaskUpdate, Calendar.getInstance().getTime(), 1000);
							try {
								rd.execute(URL_LIST_SERVICE);
							} catch (Exception e) {
								td.cancel();
								route_list.setEnabled(false);
							}
						}
					}
				});
			}else
			{Log.d("LoginUpdate","Rolling timer...");}
			
		}
	};
	//End CallPassword
	
	private void CallRestoreDefaults() {
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        msg = "Mengembalikan semuanya ke sediakala akan menghapus semua konfigurasi yang sudah diatur dan membuat aplikasi harus mendownload ulang beberapa data. Apakah anda yakin ingin mengembalikan semuanya ke setelan pabrik?";
        builder.setTitle("Peringatan");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				//Reset semua config, entah gimana caranya apus semua SharedPreferences.
				//Dan yang mudah, reset versioning jadi 0.
				Log.e("UNIQUE_FORCE_CHANGE","CHANGE to 0");
				PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.current_activity.getApplicationContext())
						.edit()
						.putString("unique_key", "0")
						.commit();
				
				CRUD_Route_Table e = new CRUD_Route_Table(FormObjectTransfer.current_activity.getApplicationContext());
				e.onUpgrade(e.getWritableDatabase(), 1, 1);
				CRUD_Route_Back_Table ed = new CRUD_Route_Back_Table(FormObjectTransfer.current_activity.getApplicationContext());
				ed.onUpgrade(e.getWritableDatabase(), 1, 1);
				
				//HEAVY TRIAL
				PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.current_activity.getApplicationContext())
						.edit()
						.clear()
						.commit();
				
				String txt = "Harap muat ulang (restart) program menggunakan menu 'Restart' untuk menerima efek perubahan.";
				Toast.makeText(FormObjectTransfer.current_activity.getApplicationContext(), txt, Toast.LENGTH_LONG).show();
				isRouteClicked = false;	
			}
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				arg0.cancel();	
			}
		});
        AlertDialog alert = builder.create();
		alert.setCanceledOnTouchOutside(true);
		alert.show();
	}
	
	private void CallExit(){
		String msg = "";
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        if(isRestart){
        	msg = "Muat ulang aplikasi?";
        }else{
        	msg = "Keluar dari aplikasi?";
        }
        builder.setTitle("Peringatan");
		builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
			// Yes
			public void onClick(DialogInterface dialog, int id) {
				if (isRestart) {
					Intent mStartActivity = new Intent(getBaseContext(), Form_Main.class);
					int mPendingIntentId = 45556;
					PendingIntent mPendingIntent = PendingIntent.getActivity(getBaseContext(), mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager mgr = (AlarmManager)getBaseContext().getSystemService(Context.ALARM_SERVICE);
					mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 400, mPendingIntent);
					System.exit(0);
				} else {
					System.out.println(" onClick ");

					try {
						FormObjectTransfer.bxl.READY_STATE = false;
						FormObjectTransfer.bxl.DisconnectPrinter();
						FormObjectTransfer.main_activity.finish(); // Hancurkan
																	// main
																	// terlebih
																	// dahulu
						FormObjectTransfer.bxl.sharedCountdown.cancel();
						FormObjectTransfer.main_activity.gls.StopGPS();
					} catch (NullPointerException e) {
						Log.w("EXIT_STATE",
								"Program called from launcher, not activity.");
					}
					FormObjectTransfer.isQuit = true;
					finish(); // Close Application method called
					System.exit(0);
				}

			}
		});
        builder.setNegativeButton("Tidak",
            new DialogInterface.OnClickListener() {
              	//No
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    isRestart = false;
                }
            });

        AlertDialog alert = builder.create();
        alert.show();
	}
	
	private void CallAbout(){
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Mobitick v.1.1");
		builder.setMessage("by Immersa Labs.\n\n" +
						   "Dukungan :\n" + 
						   "The Android Open Source Project - Android API\n" +
						   "Google - Analytics\n" +
						   "Bixolon - Bluetooth Printing Service\n" +
						   "\n"+
						   "2014. Hak cipta dilindungi oleh undang-undang."
				
				);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", 
            new DialogInterface.OnClickListener() {
        	//Yes
                public void onClick(DialogInterface dialog,int id) {
                	dialog.cancel();
                }
            });
        AlertDialog alert = builder.create();
        alert.show();
	}
}
