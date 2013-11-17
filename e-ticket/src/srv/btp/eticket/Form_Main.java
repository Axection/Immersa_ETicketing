package srv.btp.eticket;

import srv.btp.eticket.obj.Indicator;
import srv.btp.eticket.util.SystemUiHider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnDrawListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

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
	public int city_position;

	// !region Form Objects
	private Button button_action[][] = new Button[3][3];
	private Button button_left;
	private Button button_right;

	private Intent Pref;

	private Indicator[] indicators;
	private String[] city_list;
	private String[] city_display;

	private RelativeLayout top_layout;
	private RelativeLayout mid_layout;

	// listeners
	private OnTouchListener button_touch_controls;
	private OnClickListener button_click_controls;

	private OnTouchListener arrow_touch_controls;
	private OnClickListener arrow_click_controls;

	private ViewTreeObserver vto;
	private OnGlobalLayoutListener vtg;

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

		// TODO : DEBUG
		int debugNum = 32;
		top_layout.setLayoutParams(new FrameLayout.LayoutParams(
				(180 * debugNum) + 60, -2));
		CreateIndicator(debugNum);
		Log.d("debug", String.valueOf(indicators[0].txt.getLeft()));
		// END : DEBUG

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
		
		
	}

	@Override
	public void onBackPressed() {
		// Mematikan fungsi default onBackPressed
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
				v.setBackgroundResource(R.drawable.button);

				/***
				 * Serta melakukan aktivitas apa selanjutnya. Disini contohnya
				 * adalah lanjut ke activity selanjutnya.
				 * 
				 * Jangan lupa menyimpan info button mana yang ditekan agar
				 * dapat diolah oleh activity selanjutnya :D
				 * 
				 */

				Pref = new Intent(getApplicationContext(), Form_Print.class);
				startActivity(Pref);
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
				if (v == button_right)
					v.setBackgroundResource(R.drawable.button_right_active);
				else
					v.setBackgroundResource(R.drawable.button_left_active);
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

		CustomDialogControl dlg = new CustomDialogControl((Activity) this);
		dlg.show();
	}

	public void CreateIndicator(int num) {
		int left_space = 40; // Default jarak pinggir kiri
		int icon_size = 32; // Ukuran indicator

		// Pembuatan garis indikator
		ImageView line = new ImageView(this);
		line.setImageResource(R.drawable.indicator_line);
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(73
				* (2 * (num - 1)) + (32 * (num - 1)), 8);
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
					150, 68);
			balLP.setMargins(left_space - 7 + (21 * (2 * a + 1) + (136 * a)),
					12, 0, 0);
			indicators[a].balloon.setLayoutParams(balLP);
			top_layout.addView(indicators[a].balloon);

			// indikator text
			RelativeLayout.LayoutParams txtLP = new RelativeLayout.LayoutParams(
					150, 32);
			txtLP.setMargins(balLP.leftMargin
					+ (indicators[a].txt.getWidth() / 2), balLP.topMargin + 12,
					0, 0);
			indicators[a].txt.setLayoutParams(txtLP);
			top_layout.addView(indicators[a].txt);

		}

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
	
	public void CreateCityDisplay(boolean isRecreate){
		
	}
	
	/***
	 * Memindahkan Display City ke arah kiri atau kanan.
	 * @param whichArrow (0 = Panah kiri, 1 = Panah Kanan)
	 */
	public void SwitchCity(int whichArrow){
		
	}

	public void SetCityEnable(int cityIndex, boolean enable){
		
	}
	
	
}
