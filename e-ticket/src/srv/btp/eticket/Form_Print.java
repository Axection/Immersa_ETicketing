package srv.btp.eticket;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.crud.CRUD_Transaction_Queue;
import srv.btp.eticket.crud.Datafield_Route;
import srv.btp.eticket.services.BluetoothPrintService;
import srv.btp.eticket.services.GPSDataList;
import srv.btp.eticket.util.SystemUiHider;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * 
 * @see SystemUiHider
 */
public class Form_Print extends Activity {

	// !region hardcore constants
	/***
	 * Hardcoded constants
	 */
	private static final int MAX_VALUES_BOUND = 45;
	private static final int WARN_VALUES_BOUND = 8;

	// !endregion

	// !region Form Objects
	private Button button_action[][] = new Button[3][4];
	private Button button_print;
	private OnTouchListener button_touch_controls;
	private OnClickListener button_click_controls;
	private TextView txtPrintNumber;
	private TextView txtIndicator;

	private Form_Print thisForm = this;
	// !endregion

	// !region Value Calculations
	private int ticket_num = 0;

	// !endregion

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/**
		 * Ketika program ini muncul, baris-baris disini yang akan dieksekusi
		 * pertama kali.
		 */

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout_print);
		overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);

		// jangan lupa selalu informasikan ke FormObjectTransfer.java
		FormObjectTransfer.current_activity = this;

		// Deklarasi variabel variabel objek
		for (int a = 0; a < 3; a++) {
			button_action[a] = new Button[4];
		}

		/***
		 * Indexing untuk button_action : [0,0] [1,0] [2,0] [0,1] [1,1] [2,1]
		 * [0,2] [1,2] [2,2] [0,3] [1,3] [2,3]
		 */

		button_action[0][0] = (Button) this.findViewById(R.id.btnOne);
		button_action[1][0] = (Button) this.findViewById(R.id.btnTwo);
		button_action[2][0] = (Button) this.findViewById(R.id.btnThree);
		button_action[0][1] = (Button) this.findViewById(R.id.btnFour);
		button_action[1][1] = (Button) this.findViewById(R.id.btnFive);
		button_action[2][1] = (Button) this.findViewById(R.id.btnSix);
		button_action[0][2] = (Button) this.findViewById(R.id.btnSeven);
		button_action[1][2] = (Button) this.findViewById(R.id.btnEight);
		button_action[2][2] = (Button) this.findViewById(R.id.btnNine);
		button_action[0][3] = (Button) this.findViewById(R.id.btnCancel);
		button_action[1][3] = (Button) this.findViewById(R.id.btnZero);
		button_action[2][3] = (Button) this.findViewById(R.id.btnBackspace);

		button_print = (Button) this.findViewById(R.id.btnPrint);

		registerOnTouchAndClick();
		for (int aa = 0; aa < 3; aa++) {
			for (int ab = 0; ab < 4; ab++) {
				button_action[aa][ab].setOnTouchListener(button_touch_controls);
				button_action[aa][ab].setOnClickListener(button_click_controls);
			}
		}

		txtPrintNumber = (TextView) findViewById(R.id.txtNumbers);
		txtPrintNumber.setText(String.valueOf(ticket_num));

		txtIndicator = (TextView) findViewById(R.id.txtIndicator);

		button_print.setOnTouchListener(button_touch_controls);
		button_print.setOnClickListener(button_click_controls);
	}

	@Override
	public void onBackPressed() {
		finish(); // go back to the previous Activity
		overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		txtIndicator.setText(FormObjectTransfer.Kota1 + " > "
				+ FormObjectTransfer.Kota2 + " : Rp."
				+ FormObjectTransfer.harga);
	}

	/***
	 * Listener Section Disini berisi daftar Listener atas objek-objek Form
	 * untuk membaca inputan ato respon unit.
	 */

	private void registerOnTouchAndClick() {
		button_touch_controls = new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				/***
				 * Fungsi ini akan membuat simulasi "tombol ditekan"
				 */
				if (v == button_action[0][3]) { // Tombol "CANCEL"
					v.setBackgroundResource(R.drawable.button_digit_cancel_press);

				} else if (v == button_action[2][3]) { // Tombol "del"
					v.setBackgroundResource(R.drawable.button_digit_press);
				} else if (v == button_print) {
					v.setBackgroundResource(R.drawable.button_pressed);
				} else {
					v.setBackgroundResource(R.drawable.button_digit_press);

				}
				return false;
			};
		};
		button_click_controls = new OnClickListener() {
			@Override
			public void onClick(View v) {
				/***
				 * Fungsi ini akan mengembalikan warna button ke bentuk semula.
				 * serta mengeksekusi fungsi-fungsi tiap button
				 */
				if (v == button_action[0][3]) {
					v.setBackgroundResource(R.drawable.button_digit_cancel);
					onBackPressed(); // Menjalankan fungsi activity out

				} else if (v == button_action[2][3]) {
					v.setBackgroundResource(R.drawable.button_digit);

					ProceedNumber(Button2Int((Button) v));
				} else if (v == button_print) {
					v.setBackgroundResource(R.drawable.button);
					/*
					 * Di bagian sini, terjadi mekanisme : - Logging data
					 * customer - Print Data Text - dll.
					 */
					if (ticket_num > 0) {
						/*
						 * Summon Button sudah terinclude dengan print data.
						 * Namun, agar terjamin, cek Bluetooth harus dipastikan
						 * terlebih dahulu.
						 */
						if (FormObjectTransfer.bxl.BT_STATE == BluetoothPrintService.STATE_CONNECTED) {
							if (ticket_num >= WARN_VALUES_BOUND) {
								AlertDialog.Builder builder;
								builder = new AlertDialog.Builder(thisForm);
								builder.setTitle("Peringatan");
								builder.setMessage("Anda akan mencetak jumlah tiket yang cukup besar. Yakin ingin melanjutkan?\nJumlah tiket:"
										+ ticket_num);
								builder.setPositiveButton("Ya",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												FormObjectTransfer.main_activity
														.SummonButton(
																FormObjectTransfer.Kota1,
																FormObjectTransfer.Kota2,
																ticket_num,
																FormObjectTransfer.harga,
																FormObjectTransfer.harga
																		* ticket_num);
												onBackPressed();

											}
										});
								builder.setNegativeButton("Tidak",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												Toast.makeText(
														getApplicationContext(),
														"Dibatalkan.",
														Toast.LENGTH_SHORT)
														.show();
											}
										});
								AlertDialog alert = builder.create();
								alert.show();
							} else {
								FormObjectTransfer.main_activity.SummonButton(
										FormObjectTransfer.Kota1,
										FormObjectTransfer.Kota2, ticket_num,
										FormObjectTransfer.harga,
										FormObjectTransfer.harga * ticket_num);
								onBackPressed();
							}

							// Setelah ini harusnya dilakukan pencatatan histori
							// pembelian
							// TODO: INSERT QUERY PELAPORAN
							// !region AsyncTask Kirim Data
							AsyncTask<String, Integer, Boolean> asyncQuery = new AsyncTask<String, Integer, Boolean>() {

								@Override
								protected Boolean doInBackground(
										String... values) {
									if (values.length != 5) {
										Log.v("async : status", "404");
										return Boolean.FALSE;
									} else {
										String kota1, kota2;
										int ticket_num, harga, total_transaksi;

										kota1 = values[0];
										kota2 = values[1];
										ticket_num = Integer
												.parseInt(values[2]);
										harga = Integer.parseInt(values[3]);
										total_transaksi = Integer
												.parseInt(values[4]);
										// GPSDataList gpsDataList =
										// FormObjectTransfer.gdl;
										float latitude, longitude;
										latitude = PreferenceManager
												.getDefaultSharedPreferences(
														FormObjectTransfer.main_activity
																.getBaseContext())
												.getFloat("lat", 0);
										longitude = PreferenceManager
												.getDefaultSharedPreferences(
														FormObjectTransfer.main_activity
																.getBaseContext())
												.getFloat("long", 0);
										int status = postData(kota1, kota2,
												longitude, latitude,
												ticket_num, harga,
												total_transaksi);
										Log.v("async : status", status + "");
										if (status == 200)
											return true;
										else
											return false;
									}
								}

								@Override
								protected void onProgressUpdate(
										Integer... values) {
									Log.v("async : progress status",
											values[0].toString());
								};

								@Override
								protected void onPostExecute(Boolean result) {
									Log.v("async : result", result.toString());
								};

								private int postData(String kota1,
										String kota2, float longitude,
										float latitude, int jumlah_tiket,
										int harga, int total_transaksi) {
									// Create a new HttpClient and Post Header
									String jumlah_tiket_copy = jumlah_tiket
											+ "";
									// String harga_copy = harga + "";
									// String total_transaksi_copy =
									// total_transaksi + "";
									Calendar cal = Calendar.getInstance();
									SimpleDateFormat format1 = new SimpleDateFormat(
											"yy-MM-dd HH-mm-ss",
											Locale.getDefault());
									String timeNow = format1.format(cal
											.getTime());

									String URLService = PreferenceManager
											.getDefaultSharedPreferences(
													FormObjectTransfer.main_activity
															.getApplicationContext())
											.getString(
													"service_address",
													FormObjectTransfer.main_activity
															.getResources()
															.getString(
																	R.string.default_service));
									String table_name = "transaksi";
									String target_post = URLService
											+ table_name;
									HttpClient httpclient = new DefaultHttpClient();
									HttpPost httppost = new HttpPost(
											target_post);
									int code = -1;
									List<NameValuePair> nameValuePairs = null;

									try {
										// Add your data
										nameValuePairs = new ArrayList<NameValuePair>(
												2);

										CRUD_Route_Table routeTable = new CRUD_Route_Table(
												FormObjectTransfer.main_activity
														.getBaseContext());
										List<Datafield_Route> dataList = routeTable
												.getAllEntries();
										FormObjectTransfer.idKota1 = FormObjectTransfer.idKota2 = -1;
										for (Iterator<Datafield_Route> iterator = dataList
												.iterator(); iterator.hasNext();) {
											Datafield_Route datafield_Route = iterator
													.next();
											Log.v("async : iterator",
													iterator.toString());
											String debug = String.format(
													"%s %d",
													datafield_Route.get_nama(),
													datafield_Route.get_ID());
											Log.v("async : data", debug);
											if (datafield_Route.get_nama()
													.equals(kota1)) {
												FormObjectTransfer.idKota1 = (int) datafield_Route
														.get_ID();
											}

											if (datafield_Route.get_nama()
													.equals(kota2)) {
												FormObjectTransfer.idKota2 = (int) datafield_Route
														.get_ID();
											}

											if (FormObjectTransfer.idKota1 != -1
													&& FormObjectTransfer.idKota2 != -1) {
												Log.v("Async : idkota result",
														FormObjectTransfer.idKota1
																+ " "
																+ FormObjectTransfer.idKota2);
												break;
											}
										}

										nameValuePairs
												.add(new BasicNameValuePair(
												// TODO:
												// key 0 harus punya value
												// ID_trayek
														"0", 1 + ""));
										nameValuePairs
												.add(new BasicNameValuePair(
														"1",
														FormObjectTransfer.idKota1
																+ ""));
										nameValuePairs
												.add(new BasicNameValuePair(
														"2",
														FormObjectTransfer.idKota2
																+ ""));
										nameValuePairs
												.add(new BasicNameValuePair(
														"3", longitude + ""));
										nameValuePairs
												.add(new BasicNameValuePair(
														"4", latitude + ""));
										nameValuePairs
												.add(new BasicNameValuePair(
														"5", jumlah_tiket_copy));
										nameValuePairs
												.add(new BasicNameValuePair(
														"6", timeNow));
										httppost.setEntity(new UrlEncodedFormEntity(
												nameValuePairs));
										Log.v("async : debug", 1 + "");
										Log.v("async : debug",
												FormObjectTransfer.idKota1 + "");
										Log.v("async : debug",
												FormObjectTransfer.idKota2 + "");
										Log.v("async : debug",
												jumlah_tiket_copy);
										Log.v("async : debug", timeNow);
										Log.v("async : target", target_post);

										// Execute HTTP Post Request
										HttpResponse response = httpclient
												.execute(httppost);
										HttpEntity entity = response
												.getEntity();
										String responseString = EntityUtils
												.toString(entity, "iso-8859-1");
										Log.v("async : response string",
												responseString);

										code = response.getStatusLine()
												.getStatusCode();
										return code;
									} catch (ClientProtocolException e) {
										Log.d("error async query client protocol",
												e.getMessage());
										sqliteBackup(nameValuePairs);
									} catch (IOException e) {
										Log.d("error async query io exception",
												e.getMessage());
										Log.d("error async server URL",
												target_post);
										sqliteBackup(nameValuePairs);
									}
									return code;
								}

								private void sqliteBackup(
										List<NameValuePair> nameValuePairs) {
									CRUD_Transaction_Queue transactionQueue = new CRUD_Transaction_Queue(
											FormObjectTransfer.current_activity
													.getApplicationContext());
									try{
									transactionQueue
											.addTempTransaction(nameValuePairs);
									Log.v("backup",
											"Add temp transaction success");
									}catch(Exception e){
										Log.e("backup",
												"Add temp transaction FAILED");
									}

								};
							};
							asyncQuery.execute(new String[] {
									(String) FormObjectTransfer.Kota1,
									(String) FormObjectTransfer.Kota2,
									ticket_num + "",
									FormObjectTransfer.harga + "",
									(FormObjectTransfer.harga * ticket_num)
											+ "" });
							// !endregion

						} else {
							FormObjectTransfer.bxl.ConnectPrinter();
							Toast.makeText(
									getApplicationContext(),
									"Bluetooth tidak sedang dalam keadaan tersambung.",
									Toast.LENGTH_SHORT).show();
						}
					} else {
						Toast.makeText(getApplicationContext(),
								"Jumlah tiket yang anda masukkan tidak benar.",
								Toast.LENGTH_SHORT).show();
					}
				} else {
					v.setBackgroundResource(R.drawable.button_digit);
					ProceedNumber(Button2Int((Button) v));
				}
			}
		};

	}

	private int Button2Int(Button b) {
		if (b.getId() == R.id.btnOne)
			return 1;
		if (b.getId() == R.id.btnTwo)
			return 2;
		if (b.getId() == R.id.btnThree)
			return 3;
		if (b.getId() == R.id.btnFour)
			return 4;
		if (b.getId() == R.id.btnFive)
			return 5;
		if (b.getId() == R.id.btnSix)
			return 6;
		if (b.getId() == R.id.btnSeven)
			return 7;
		if (b.getId() == R.id.btnEight)
			return 8;
		if (b.getId() == R.id.btnNine)
			return 9;
		if (b.getId() == R.id.btnZero)
			return 0;
		return -1;
	}

	private void ProceedNumber(int addition) {
		/***
		 * Fungsi ini bertujuan untuk memprosesi nilai input yang ditekan. Jadi
		 * contoh :
		 * 
		 * ticket_num = 3 <tombol 5 ditekan> ticket_num = 35
		 * 
		 * nilai input -1 adalah delete. sehingga dari 35 mundur menjadi 3.
		 * 
		 * Batas default nya adalah int MAX_VALUES_BOUND = 99;
		 */

		int curVal = ticket_num;
		Log.d("ProceedNumber", String.valueOf(curVal));
		if (addition != -1) {
			curVal *= 10;
			if (curVal > MAX_VALUES_BOUND) {
				curVal = MAX_VALUES_BOUND;
			} else
				curVal += addition;
		} else {
			curVal /= 10;
		}
		ticket_num = curVal;
		Log.d("ProceedNumberEnd", String.valueOf(curVal));
		txtPrintNumber.setText(String.valueOf(ticket_num));
	}

}
