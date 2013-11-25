package srv.btp.eticket;

import srv.btp.eticket.services.BluetoothPrintService;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.text.TextUtils;
import android.util.Log;

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

	private BluetoothPrintService btx;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//Must Have
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		setupSimplePreferencesScreen();
		
		
		//Preference for Bluetooth List
		bluetoothList = (ListPreference) findPreference("bluetooth_list");
		btx = new BluetoothPrintService(this,null);
		if(btx.FindPrinters() == 0){
			Log.d("BTX",btx.toString() + " address access : " + btx.getBtAddr().size() );
			btListAddress = new CharSequence[btx.getBtAddr().size()];
			btListNames = new CharSequence[btx.getBtAddr().size()];
			Log.d("BTX",btListAddress.toString() + " " + btListNames + " access address");
			int a = 0;
			for(String s : btx.getBtAddr()){
				//Log.d("BTX",s + " get address");
				btListNames  [a] = s.substring(0, s.indexOf("|"));
				btListAddress[a] =  s.substring(s.indexOf("|")+1,s.length());
				Log.d("BTXData",btListAddress[a] + " ... " + btListNames[a]);
				a++;
			}
			bluetoothList.setEntries(btListNames);
			bluetoothList.setEntryValues(btListAddress);
			Log.d("BTX",bluetoothList.toString() +  " post address");
			bluetoothList.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					//debug
					for(int aa=0;aa<btListNames.length;aa++){
						Log.d("PrefBTXData",bluetoothList.getEntries()[aa] + " = " + bluetoothList.getEntryValues()[aa]);
					}
					bluetoothList.setEntries(btListNames);
					bluetoothList.setEntryValues(btListAddress);
					return false;
				}
			});
			
			
			
		}else{
			bluetoothList.setEnabled(false);
		}

	}

	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
		//LEMPAR SEMUA VALUE! >_<
	}
	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
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
		
		//Tambahan fitur keluar
		Preference p = findPreference("pref_quit");
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
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);

			} else {
				// For all other preferences, set the summary to the value's
				// simple string representation.
				preference.setSummary(stringValue);
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

	private void CallExit(){
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Peringatan");
		builder.setMessage("Keluar dari aplikasi?");
        builder.setCancelable(false);
        builder.setPositiveButton("Ya", 
            new DialogInterface.OnClickListener() {
        	//Yes
                public void onClick(DialogInterface dialog,int id) {
                    System.out.println(" onClick ");
                    FormObjectTransfer.main_activity.finish(); //Hancurkan main terlebih dahulu
                    FormObjectTransfer.isQuit = true;
                    FormObjectTransfer.bxl.sharedCountdown.cancel();
                    FormObjectTransfer.main_activity.gls.StopGPS();
                    finish(); // Close Application method called
                    
                    
                }
            });
        builder.setNegativeButton("Tidak",
            new DialogInterface.OnClickListener() {
              	//No
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();
	}
	private void CallAbout(){
		AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Mobile Ticketing v.1.00");
		builder.setMessage("Dibuat oleh Immersa Labs.\n\n" +
						   "Dukungan :\n" + 
						   "The Android Open Source Project - Android API\n" +
						   "Google - Analytics dan Statistics\n" +
						   "Bixolon - Bluetooth Printing Service\n" +
						   "\n"+
						   "2013. Some rights reserved."
				
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
