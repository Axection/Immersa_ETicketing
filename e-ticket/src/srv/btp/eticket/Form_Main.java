package srv.btp.eticket;

import srv.btp.eticket.util.SystemUiHider;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Form_Main extends Activity {

	// !region hardcore constants
	/***
	 * Hardcoded constants
	 */
	private static final boolean AUTO_HIDE = true;
	private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
	private static final boolean TOGGLE_ON_CLICK = true;
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
	// !endregion

	// !region Form Objects
	private Button button_action[][] = new Button[3][3];
	private Button button_left;
	private Button button_right;
	private OnTouchListener button_touch_controls;
	private OnClickListener button_click_controls;

	private OnTouchListener arrow_touch_controls;
	private OnClickListener arrow_click_controls;

	private Intent Pref;

	// !endregion

	/***
	 * Listener Section Disini berisi daftar Listener atas objek-objek Form
	 * untuk membaca inputan ato respon unit.
	 */
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * Ketika program ini muncul, baris-baris disini yang akan dieksekusi
		 * pertama kali.
		 */

		//baris wajib :D
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_form_main);
		overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
		
		//jangan lupa selalu informasikan ke FormObjectTransfer.java
		FormObjectTransfer.main_activity = this;
		FormObjectTransfer.current_activity = this;

		// Deklarasi variabel variabel objek
		for (int a = 0; a < 3; a++) {
			button_action[a] = new Button[3];
		}

		/***
		 * Indexing untuk button_action : [0,0] [1,0] [2,0] [0,1] [1,1] [2,1]
		 * [0,2] [1,2] [2,2]
		 */

		button_action[0][0] = (Button) this.findViewById(R.id.btnTopLeft);
		button_action[1][0] = (Button) this.findViewById(R.id.btnTopTop);
		button_action[2][0] = (Button) this.findViewById(R.id.btnTopRight);
		button_action[0][1] = (Button) this.findViewById(R.id.btnMidLeft);
		button_action[1][1] = (Button) this.findViewById(R.id.btnMidMid);
		button_action[2][1] = (Button) this.findViewById(R.id.btnMidRight);
		button_action[0][2] = (Button) this.findViewById(R.id.btnBotLeft);
		button_action[1][2] = (Button) this.findViewById(R.id.btnBotMId);
		button_action[2][2] = (Button) this.findViewById(R.id.btnBotRight);

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

	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
	}


	@Override
	public void onBackPressed() {
		overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	}

	@Override
	public void onResume() {
		super.onResume();
		FormObjectTransfer.current_activity = this;
	}

	/***
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
	
	public void SummonButton(CharSequence Kota1, CharSequence Kota2, int qty, int harga, int total){
		//TODO : DIGANTI JADI CUSTOM NOTIF ALERT
		FormObjectTransfer.Kota1 = Kota1;
		FormObjectTransfer.Kota2 = Kota2;
		FormObjectTransfer.qty = qty;
		FormObjectTransfer.harga = harga;
		FormObjectTransfer.total = total;
		
		CustomDialogControl dlg = new CustomDialogControl((Activity)this);
		dlg.show();
	}
	
	
}
