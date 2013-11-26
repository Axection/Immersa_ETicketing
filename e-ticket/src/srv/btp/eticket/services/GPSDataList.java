package srv.btp.eticket.services;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import android.preference.PreferenceManager;

public class GPSDataList {

	/*** 
	 * Sampling
	 *
	 * {\
                                0, //null rambutan
                                8000, //rambutan bekasi
                                5000, //bekasi cisarua
                                10000,//cisarua garut
                                5000, //garut sukabumi
                                8000, //sukabumi sumedang
                                8000, //sumedang tegalega
                                5000, //tegalega padalarang
                                5000, //padalarang kopo
                                5000, //kopo buahbatu
                                10000, //buahbatu cielunyi
                                15000, //cileunyi tasikamalaya
                                0, //tasikmalaya, null
                                };
	 */
	
	//variabel helper
	private String[] kotaListForward;
	private int listSizeForward;
	private int hargaParsialForward[];
	
	private String[] kotaListReverse;
	private int listSizeReverse;
	private int[] hargaParsialReverse;
	
	//properti publik
	public String[] kotaList;
	public int listSize;
	public int hargaParsial[];
	public boolean isReversed = false;
	//Layanan
	ServerDatabaseService sdd;
	String ServiceBaseURL = PreferenceManager.getDefaultSharedPreferences(
			FormObjectTransfer.main_activity.getBaseContext()).getString(
					"service_address", 
					FormObjectTransfer.main_activity.getResources().getString(
							R.string.default_service));
	
	
	
	public void ReverseTrack(){
		if(isReversed){
			kotaList = kotaListForward;
			listSize = listSizeForward;
			hargaParsial = hargaParsialForward;
			isReversed = false;
		}else{
			kotaList = kotaListReverse;
			listSize = listSizeReverse;
			hargaParsial = hargaParsialReverse;
			isReversed = true;
		}
	}
	
	public void getDataFromJSON(){
		/*
		 * Disini program akan melakukan get data 3x.
		 * Yaitu : 
		 * - Cek versioning. Apabila lebih tinggi, diupdate. Apabila tidak, langsung load dari SQLite.
		 * - Download Update. Jika diupdate, maka data akan disimpan ke SQLite, jika tidak, skip.
		 * - Download trayek reverse. Jika diupdate, data reverse juga disimpan ke SQLite, jika tidak, skip.
		 * 
		 * Requires : ServerDatabaseService.java
		 * GSON library
		 * 
		 */
		
		//session 1
		sdd = new ServerDatabaseService();
		String URL_LIST_SERVICE[] = {
				ServerDatabaseService.URL_SERVICE_VERSION_CHECK,
				ServerDatabaseService.URL_SERVICE_FORWARD,
				ServerDatabaseService.URL_SERVICE_REVERSE,
				ServerDatabaseService.URL_SERVICE_PRICE_FORWARD,
				ServerDatabaseService.URL_SERVICE_PRICE_REVERSE
		};
		Timer td = new Timer(true);
		td.schedule(TaskUpdate, Calendar.getInstance().getTime(), 1000);
		sdd.execute(URL_LIST_SERVICE);

		
	}
	protected TimerTask TaskUpdate = new TimerTask() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(ServerDatabaseService.isDone){
				this.cancel();
				generateData();
			}
			
		}
	};
	
	public void generateData(){
		/*
		 * Tugas dari fungsi ini adalah menggenerate data dari SQLite ke array string.
		 * Fungsi SQLite sudah didefine di masing-masing kelas.
		 * 
		 * Setelah itu, data akan masuk ke data forward berikut reverse.
		 */
		FormObjectTransfer.main_activity.PrepareCityList();
	}
}
