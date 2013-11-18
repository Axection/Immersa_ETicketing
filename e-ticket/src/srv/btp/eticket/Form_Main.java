package srv.btp.eticket;

import srv.btp.eticket.obj.CityList;
import srv.btp.eticket.obj.Indicator;
import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.util.SystemUiHider;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
@SuppressLint("NewApi")
public class Form_Main extends Activity {

	//constant related
	
	// public related value
	public int tmpInt;
	public int city_position; //Bukan Indeks
	public int city_max_position; //Bukan Indeks
	public int city_real_position = 1; //Berubah mengikuti GPS

	// !region Form Objects
	private Button button_action[][] = new Button[3][3];
	private Button button_left;
	private Button button_right;

	private Button button_pref;
	
	private Intent intentPrint;
	private Intent intentPref;

	private Indicator[] indicators;
	private CityList[] city_list;
	private CityList[] city_display;

	private RelativeLayout top_layout;
	private RelativeLayout mid_layout;
	
	//Service Objects
	BluetoothPrintService btx;

	// listeners
	private OnTouchListener button_touch_controls;
	private OnClickListener button_click_controls;

	private OnTouchListener arrow_touch_controls;
	private OnClickListener arrow_click_controls;


	// !endregion

	/***
	 * Listener Section disini berisi daftar Listener atas objek-objek Form
	 * untuk membaca inputan ato respon unit.
	 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * Ketika program ini muncul, baris-baris disini yang akan dieksekusi
		 * pertama kali.
		 */

		// baris wajib :D
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_form_main);
		overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

		// jangan lupa selalu informasikan ke FormObjectTransfer.java
		FormObjectTransfer.main_activity = this;
		FormObjectTransfer.current_activity = this;

		// Deklarasi variabel variabel objek
		for (int a = 0; a < 3; a++) {
			button_action[a] = new Button[3];
		}

		/*
		 * Indexing untuk button_action : [0,0] [1,0] [2,0] [0,1] [1,1] [2,1]
		 * [0,2] [1,2] [2,2]
		 */
		
		button_action[0][0] = (Button) this.findViewById(R.id.btnTopLeft); //0,0 = 1
		button_action[1][0] = (Button) this.findViewById(R.id.btnTopTop);  //1,0 = 2
		button_action[2][0] = (Button) this.findViewById(R.id.btnTopRight);//2,0 = 3
		button_action[0][1] = (Button) this.findViewById(R.id.btnMidLeft); //0,1 = 4
		button_action[1][1] = (Button) this.findViewById(R.id.btnMidMid);  //1,1 = 5
		button_action[2][1] = (Button) this.findViewById(R.id.btnMidRight);//2,1 = 6
		button_action[0][2] = (Button) this.findViewById(R.id.btnBotLeft); //0,2 = 7
		button_action[1][2] = (Button) this.findViewById(R.id.btnBotMId);  //1,2 = 8
		button_action[2][2] = (Button) this.findViewById(R.id.btnBotRight);//2,2 = 9

		registerOnTouchAndClick();
		for (int aa = 0; aa < 3; aa++) {
			for (int ab = 0; ab < 3; ab++) {
				button_action[aa][ab].setOnTouchListener(button_touch_controls);
				button_action[aa][ab].setOnClickListener(button_click_controls);
			}
		}
		button_left = (Button) this.findViewById(R.id.btnLeftSide);
		button_left.setOnTouchListener(arrow_touch_controls);
		button_left.setOnClickListener(arrow_click_controls);

		button_right = (Button) this.findViewById(R.id.btnRightSide);
		button_right.setOnTouchListener(arrow_touch_controls);
		button_right.setOnClickListener(arrow_click_controls);

		// layout set
		top_layout = (RelativeLayout) findViewById(R.id.top_linear);
		mid_layout = (RelativeLayout) findViewById(R.id.relInside);

		//preferences
		button_pref = (Button)this.findViewById(R.id.btn_config);
		this.PreparePreferences();
		
		//service initialization
		btx = new BluetoothPrintService(this);
		Toast.makeText(this, "Menyambung ke printer bluetooth...", Toast.LENGTH_SHORT).show();		
	}

	private Button Int2Button(int num) {
		switch(num){
		case 1:
			return button_action[0][0];
		case 2:
			return button_action[1][0];
		case 3:
			return button_action[2][0];
		case 4:
			return button_action[0][1];
		case 5:
			return button_action[1][1];
		case 6:
			return button_action[2][1];
		case 7:
			return button_action[0][2];
		case 8:
			return button_action[1][2];
		case 9:
			return button_action[2][2];
			default:
				return null;
		}
	}
	private int ButtonID2Int(int button_id){
		switch(button_id){
		case R.id.btnTopLeft:
			return 1;
		case R.id.btnTopTop:
			return 2;
		case R.id.btnTopRight:
			return 3;
		case R.id.btnMidLeft:
			return 4;
		case R.id.btnMidMid:
			return 5;
		case R.id.btnMidRight:
			return 6;
		case R.id.btnBotLeft:
			return 7;
		case R.id.btnBotMId:
			return 8;
		case R.id.btnBotRight:
			return 9;
			default:
				return 0;
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		/**
		 * Berbagai fungsi ada yang tidak berjalan semestinya di onCreate.
		 * Namun fungsi itu perlu berjalan ketika startup.
		 * Maka dari itu pindahkan fungsi2 tersebut disini.
		 * 
		 * Namun perlu diperhatikan, callback ini selalu dipanggil ketika muncul,
		 * atau ketika activity selanjutnya "pulang" ke activity ini.
		 **/
	}

	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		btx._setBtAddr(		
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
						.getString("bluetooth_list", "00:00:00:00:00:00")
					);
		Log.d("BluetoothAddressLoader",btx.btSelectedAddr);
		int retval = btx.ConnectPrinter();
		if (retval == 0) {
			Toast.makeText(this, "Sambungan ke Bluetooth berhasil.", Toast.LENGTH_SHORT).show();
			FormObjectTransfer.bxl = btx;
		} else {
			Toast.makeText(this, 
					"Kesalahan terjadi pada sambungan bluetooth. Mohon diperiksa kembali sambungan bluetooth di konfigurasi."
					+ "\n" + 
					"Error Code: " + retval,  
					Toast.LENGTH_LONG)
				.show();
			
		}
		PrepareCityList();
	}

	@Override
	public void onBackPressed() {
		// Mematikan fungsi default onBackPressed
		DoNothing();
		// Animasi
		overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	}

	@Override
	public void onResume() {
		super.onResume();
		FormObjectTransfer.current_activity = this;
	}

	/*
	 * Additional Function Disini berisi daftar method atas objek-objek Form
	 * untuk tambahan fungsi.
	 */
	
	private void registerOnTouchAndClick() {
		button_touch_controls = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				/***
				 * Fungsi ini akan membuat simulasi "tombol ditekan"
				 */
				v.setBackgroundResource(R.drawable.button_pressed);
				return false;
			};
		};
		button_click_controls = new OnClickListener() {
			@Override
			public void onClick(View v) {
				/***
				 * Fungsi ini akan mengembalikan warna button ke bentuk semula.
				 */
				Button b = (Button)v;
				v.setBackgroundResource(R.drawable.button);

				/***
				 * Serta melakukan aktivitas apa selanjutnya. Disini contohnya
				 * adalah lanjut ke activity selanjutnya.
				 * 
				 * Jangan lupa menyimpan info button mana yang ditekan agar
				 * dapat diolah oleh activity selanjutnya :D
				 * 
				 */
				//TODO : Pindahkan data-data ke FormObjekTransfer untuk reusability di FormPrint
				FormObjectTransfer.Kota1 = city_list[city_real_position-1].Nama;
				FormObjectTransfer.Kota2 = b.getText();
				FormObjectTransfer.harga = 0;
				//Tarif Loops
				for(int a=city_real_position-1;a<city_position-1+ButtonID2Int(b.getId());a++){
					if(a==city_real_position-1){
						FormObjectTransfer.harga+= city_list[a].TarifKanan;
					}else if(a==city_position-2+ButtonID2Int(b.getId())){
						FormObjectTransfer.harga+=city_list[a].TarifKiri;
					}else
					FormObjectTransfer.harga+=city_list[a].TarifKiri + city_list[a].TarifKanan;
				}
				
				//End Tarif
				
				
				//Intent Moving
				intentPrint = new Intent(getApplicationContext(), Form_Print.class);
				startActivity(intentPrint);
				overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
			}
		};
		arrow_touch_controls = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (v == button_right)
					v.setBackgroundResource(R.drawable.button_right_pressed);
				else
					v.setBackgroundResource(R.drawable.button_left_pressed);

				return false;
			}
		};
		arrow_click_controls = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (v == button_right){
					v.setBackgroundResource(R.drawable.button_right_active);
					SwitchCity(1);}
				else{
					v.setBackgroundResource(R.drawable.button_left_active);
					SwitchCity(0);}
				
			}
		};
	}

	public void SummonButton(CharSequence Kota1, CharSequence Kota2, int qty,
			int harga, int total) {
		FormObjectTransfer.Kota1 = Kota1;
		FormObjectTransfer.Kota2 = Kota2;
		FormObjectTransfer.qty = qty;
		FormObjectTransfer.harga = harga;
		FormObjectTransfer.total = total;
		String GeneratedID = "RNDIDXX"; //Siapkan modul untuk getGeneratedID
		btx.PrintText(GeneratedID, Kota1.toString(), Kota2.toString(), qty, harga);
		CustomDialogControl dlg = new CustomDialogControl((Activity) this);
		dlg.show();
	}

	public void CreateIndicator(int num) {
		int left_space = 40; // Default jarak pinggir kiri
		int icon_size = 32; // Ukuran indicator
		int block_indicator_size = 150; //Ukuran per indikator

		// Pembuatan garis indikator
		ImageView line = new ImageView(this);
		line.setImageResource(R.drawable.indicator_line);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(73
				* (2 * (num - 1)) + (icon_size * (num - 1)), 8);
		lp.setMargins(left_space + 53 + 24, 98, 0, 0);
		line.setLayoutParams(lp);
		line.setScaleType(ScaleType.CENTER_CROP);
		top_layout.addView(line);

		// Pembuatan Indicator
		indicators = new Indicator[num];
		for (int a = 0; a < num; a++) {
			indicators[a] = new Indicator("test_Debug",
					this.getApplicationContext());

			// indikator bundar
			RelativeLayout.LayoutParams indLP = new RelativeLayout.LayoutParams(
					icon_size, icon_size);
			indLP.setMargins(left_space + (73 * (2 * a + 1) + (icon_size * a)),
					86, 0, 0);
			indicators[a].img.setLayoutParams(indLP);
			top_layout.addView(indicators[a].img);

			// indikator balon
			RelativeLayout.LayoutParams balLP = new RelativeLayout.LayoutParams(
					block_indicator_size, 68);
			balLP.setMargins(left_space - 7 + (21 * (2 * a + 1) + (136 * a)),
					12, 0, 0);
			indicators[a].balloon.setLayoutParams(balLP);
			top_layout.addView(indicators[a].balloon);

			// indikator text
			RelativeLayout.LayoutParams txtLP = new RelativeLayout.LayoutParams(
					block_indicator_size, icon_size);
			txtLP.setMargins(balLP.leftMargin
					+ (indicators[a].txt.getWidth() / 2), balLP.topMargin + 12,
					0, 0);
			indicators[a].txt.setLayoutParams(txtLP);
			top_layout.addView(indicators[a].txt);

		}

	}

	
	public void PreparePreferences(){
		button_pref.setOnClickListener(new OnClickListener() {
	
			@Override
			public void onClick(View arg0) {
				button_pref.setBackgroundResource(R.drawable.button_config);
				intentPref = new Intent(getApplicationContext(), AppPreferences.class);
				startActivity(intentPref);
			}
		});
		button_pref.setOnTouchListener(new OnTouchListener() {	
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				button_pref.setBackgroundResource(R.drawable.button_config_pressed);
				return false;
			}
		});
		
	}
	
	public int getTextWidth(String text, Paint paint) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		int width = bounds.left + bounds.width();
		return width;
	}

	public int getTextHeight(String text, Paint paint) {
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);
		int height = bounds.bottom + bounds.height();
		return height;
	}
	
	public void PrepareCityList(){
		//TODO: ngambil data dari SQLite menuju daftar kota.
		//TODO: Untuk sementara, gunakan data dummy

		
		String[] namaKota = /* Masih pakai data dummy*/ 
			{"Rambutan", "Bekasi", "Cisarua", "Garut", "Sukabumi", "Sumedang", 
				"Tegalega", "Padalarang", "Kopo", "Buah Batu", "Cileunyi",
				 "Tasikmalaya"};
		int dataSize = namaKota.length;//12;
		int hargaParsial[] = { /*Masih pakai data dummy*/
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
		//!region dummy
		city_list = new CityList[dataSize];
		city_max_position = dataSize;
		top_layout.setLayoutParams(new FrameLayout.LayoutParams(
				(180 * dataSize) + 60, -2)); //180, 60, -2 adalah konstanta tetap panjang layout. jangan diubah.
		CreateIndicator(dataSize);
		city_display = new CityList[dataSize<9 ? dataSize : 9];
		Log.d("debug", String.valueOf(indicators[0].txt.getLeft()));
		for(int a = 0; a<dataSize;a++){
			indicators[a].txt.setText(namaKota[a]);
			city_list[a] = new CityList();
			city_list[a].Nama = namaKota[a];
			city_list[a].Priority = a;
			city_list[a].TarifKiri = hargaParsial[a]/2;
			city_list[a].TarifKanan = hargaParsial[a+1]/2;
 		}
		button_left.setBackgroundResource(R.drawable.button_left_passive);
		button_left.setEnabled(false);
		
		int initializationValue = 3; //TODO: Nanti di load dari GPS irisan lokasi.
		SetCityEnable(initializationValue); 
		city_position = 1;
		for(int a = 0;a<city_display.length;a++){
			city_display[a] = city_list[a];
		}
		CreateCityDisplay(city_display);

		//!endregion
	}
	
	
	public void CreateCityDisplay(CityList[] cityList){
			//TODO: Konversi data list string jadi City Display
			//Secara tidak langsung, Create City Display membuat menu indikator
			final int size = 9;
			int indicator = 9;
			if(cityList.length<size){
				indicator = cityList.length;
				button_right.setBackgroundResource(R.drawable.button_right_passive);
				button_right.setEnabled(false);
			}
			for(int a = 0; a < indicator; a++){
				Int2Button(a+1).setText(cityList[a].Nama);
				if(cityList[a].isNotPassed){
				Int2Button(a+1).setBackgroundResource(R.drawable.button);
				Int2Button(a+1).setEnabled(true);
				}else{
					Int2Button(a+1).setBackgroundResource(R.drawable.button_disabled);
					Int2Button(a+1).setEnabled(false);
				}
			}
			for(int a = indicator;a<size;a++){
				Int2Button(a+1).setText("-");
				Int2Button(a+1).setBackgroundResource(R.drawable.button_passive);
				Int2Button(a+1).setEnabled(false);
			}
		}
	
	/***
	 * Memindahkan Display City ke arah kiri atau kanan.
	 * @param whichArrow (0 = Panah kiri, 1 = Panah Kanan)
	 */
	public void SwitchCity(int whichArrow) {
		// TODO: Mekanisme data list string ketika klik kiri kanan
		if (whichArrow == 0 && city_position > 9) {
			city_display = new CityList[9];
			city_position -= 9;
			for (int a = city_position; a < city_position + 9; a++) {
				city_display[a-city_position] = city_list[a-1];
			}
			if (!(city_position > 1)) {
				button_left
						.setBackgroundResource(R.drawable.button_left_passive);
				button_left.setEnabled(false);

				button_right
						.setBackgroundResource(R.drawable.button_right_active);
				button_right.setEnabled(true);
			}
		} else if (whichArrow == 1 && city_position < city_max_position) {
			Log.d("DEBUG_POSITION", city_position + " " + city_max_position + " real : "+ city_real_position);
			city_position += 9;
			city_display = new CityList[city_max_position-city_position<=9?city_max_position-city_position+1 : 9];
			for (int a = city_position; a < city_position+city_display.length; a++) {
				city_display[a-city_position] = city_list[a-1];
			}
			if (city_max_position - city_position<=9) {
				button_right
						.setBackgroundResource(R.drawable.button_right_passive);
				button_right.setEnabled(false);

				button_left
						.setBackgroundResource(R.drawable.button_left_active);
				button_left.setEnabled(true);
			}
		}
		CreateCityDisplay(city_display);
	}

	public void SetCityEnable(int cityIndex){
		if(cityIndex > city_max_position){
			//Fail ketika inputan melebihi Index
			return;
		}
		city_real_position = cityIndex;
		//TODO: Mekanisme Button On Off
		//Sebelumnya, ingat bahwa apabila cityIndex > 0, maka index sebelumnya
		//berubah menjadi disabled.
		for(int a=0;a<cityIndex-1;a++){
			indicators[a].setEnabled(2);
		city_list[a].isNotPassed = false;
		}
		indicators[cityIndex-1].setEnabled(1);
		city_list[cityIndex-1].isNotPassed = false;
		for(int aa=cityIndex;aa<city_max_position;aa++){
			indicators[cityIndex].setEnabled(0);
			city_list[cityIndex].isNotPassed = true;
		}
	}
	/***
	 * Fungsi ini tidak melakukan apa-apa :D
	 */
	public void DoNothing(){
		return;
	}
	
}
