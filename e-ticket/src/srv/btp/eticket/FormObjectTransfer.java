package srv.btp.eticket;

import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.services.GPSDataList;
import srv.btp.eticket.services.GPSLocationService;
import android.app.Activity;

public class FormObjectTransfer {
	/*
	 * Kelas ini bertujuan untuk sentral tukar informasi antar Activity agar tidak
	 * ada activity yang saling lempar data secara direct.
	 */
	public static Activity last_activity;
	public static Activity current_activity;
	public static Form_Main main_activity;
	public static CharSequence Kota1, Kota2;
	public static int qty, harga, total;
	public static String btAddress;
	public static BluetoothPrintService bxl;
	public static GPSDataList gdl;
	public static int CURRENT_STATE;
	public static boolean isBTConnected = false;
	public static boolean isGPSConnected = false;
	public static boolean isInitalizationState = true;
	public static boolean isQuit = false;
	/***
	 * Daftar STATE :
	 * 0 - 
	 * 1 - 
	 * 2 - 
	 * 3 - 
	 * 4 - 
	 * 
	 */
	
	
}
