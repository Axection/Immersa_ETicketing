package srv.btp.eticket.services;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;

/***
 * BluetoothPrintService.java
 * 
 * Kelas ini dibuat untuk melakukan sambungan, komunikasi serta manajemen dengan
 * printer Bluetooth yang ada. API disediakan di CD.
 * 
 * Skema pemakaian : - Sebelumnya user sudah harus melakukan pairing dengan
 * printernya - Bluetooth Enable - Lookup printer - Idling system, tunggu sampai
 * ada command print - Looping tes Reconnect
 * 
 * Info menyusul.
 */
public class BluetoothPrintService {

	/***
	 * btAddr = Bluetooth Address. Nilai ini akan berubah secara otomatis
	 * setelah fungsi {@link FindPrinter} dieksekusi.
	 */
	private List<String> btAddr;
	public String btSelectedAddr;
	BluetoothAdapter mBluetoothAdapter;

	String dataPrint = "";
	int lastStatus = 0;

	BixolonPrinter bxl;
	private int REQUEST_ENABLE_BT;
	
	public int BT_STATE;
	public static final int STATE_CONNECTED = 0;
	public static final int STATE_CONNECTING = 1;
	public static final int STATE_DISCONNECTED = 2;
	
	public static final int RECONNECT_TIMEOUT = 10000;
	
	
	private Activity selected_activity;

	public BluetoothPrintService(Activity c) {
		bxl = new BixolonPrinter(c.getBaseContext(), BLUETOOTH_HANDLER, null);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		selected_activity = c;
		btAddr = new ArrayList<String>();
	}

	/***
	 * Fungsi otomatis menemukan printer terdekat. Fungsi ini akan mengubah
	 * nilai {@link btAddr}
	 * 
	 * @return Nilai code hasil ditemukan atau tidak. (0= Sukses, 1= Gagal)
	 * 
	 */
	public int FindPrinters() {
		btAddr.clear();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
			// Loop through paired devices
			for (BluetoothDevice device : pairedDevices) {
				btAddr.add(device.getName() + "|" + device.getAddress());
				// if(device.getBluetoothClass().getDeviceClass() ==
				// BluetoothClass.Device.Major.IMAGING){
				// return 0;
				// }
			}
			return 0;
		}
		return 1;

	}

	public void ConnectPrinter() {
		boolean b = EnableBT();
		if (!b)
			return;
		bxl.connect(btSelectedAddr);
	}
	
	public void DisconnectPrinter(){
		bxl.disconnect();
		
	}

	private final Handler BLUETOOTH_HANDLER = new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case BixolonPrinter.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BixolonPrinter.STATE_CONNECTING:
					Toast.makeText(selected_activity.getApplicationContext(), "Menyambung ke printer bluetooth...", Toast.LENGTH_SHORT).show();	
					BT_STATE = STATE_CONNECTING;
					break;
				case BixolonPrinter.STATE_CONNECTED:
					Toast.makeText(selected_activity.getApplicationContext(), "Sambungan ke Bluetooth berhasil.", Toast.LENGTH_SHORT).show();
					BT_STATE = STATE_CONNECTED;
					break;
				case BixolonPrinter.STATE_NONE:
			        Toast.makeText(selected_activity.getApplicationContext(), 
			        			"Sambungan printer terputus. Menyambung kembali dalam waktu " + (RECONNECT_TIMEOUT/1000) +" detik...\n",
			        			Toast.LENGTH_LONG)
			        			.show();
					BT_STATE = STATE_DISCONNECTED;
			        CountDownTimer ctd = new CountDownTimer(RECONNECT_TIMEOUT,1000){
						@Override
						public void onFinish() {
							ConnectPrinter();
						}
						@Override public void onTick(long millisUntilFinished) {}
					};
					ctd.start();
					break;
				}
				break;
			case BixolonPrinter.MESSAGE_DEVICE_NAME:
				String connectedDeviceName = msg.getData().getString(
						BixolonPrinter.KEY_STRING_DEVICE_NAME);
				break;
			case BixolonPrinter.MESSAGE_TOAST:
				Toast.makeText(
						selected_activity.getApplicationContext(),
						msg.getData()
								.getString(BixolonPrinter.KEY_STRING_TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
			return true;
		}
	});

	public boolean EnableBT() {
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			selected_activity.startActivityForResult(enableBtIntent,
					REQUEST_ENABLE_BT);
		} else {
			return true;
		}
		if (REQUEST_ENABLE_BT == Activity.RESULT_OK) {
			return true;
		}
		return false;
	}

	public boolean PrintText(String ID, String AsalKota, String TujuanKota,
			int tiketNum, int Harga) {
		// Buat persiapan terlebih dahulu untuk format
		String formattedString = "";
		Calendar c = Calendar.getInstance();
		/***
		 * Sebelumnya, ada sedikit penjelasan dengan format yang dipakai dalam
		 * print kali ini. ID = ID serialisasi dari tiket. Dapatkan serialisasi
		 * ID nanti dari kelas Serialisasi. AsalKota = Kota asal. Format String
		 * saja. TujuanKota = Kota tujuan. tiketNum = Jumlah tiket yang di
		 * print. Ingat, sistematikanya adalah multiprint. Artinya, Apabila
		 * dipesan 3, maka diprint tiga kali. bukan ditulis angka 3. Harga =
		 * Harga satuan dari per tiket. Tidak perlu print total.
		 * 
		 * Gambaran print:
		 * 
		 * ************************* ****MOBILE TICKETING*****
		 * ************************* <hari>, <tgl> Waktu : <jam> NOMOR TIKET:
		 * <ID>
		 * 
		 * DETAIL TIKET ANDA ASAL : <asal> TUJUAN <tujuan>
		 * 
		 * [<harga>]
		 * 
		 * ************************* TERIMA KASIH Mobile Ticketing oleh Immersa
		 * Labs 2013 *************************
		 * 
		 */
		String theDate = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
				Locale.US)
				+ ", "
				+ c.get(Calendar.DAY_OF_MONTH)
				+ " "
				+ c.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US)
				+ " " + c.get(Calendar.YEAR);
		String theTime = c.get(Calendar.HOUR_OF_DAY) + ":"
				+ c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);

		// Desain string ke printer
		formattedString += "********************************\n"
				+ "********MOBILE TICKETING********\n"
				+ "********************************\n"
				+ theDate
				+ "\n"
				+ "WAKTU: "
				+ theTime
				+ "\n"
				+ "NOMOR TIKET: "
				+ ID
				+ "\n"
				+ "\n"
				+ "DETAIL TIKET ANDA\n"
				+ "ASAL: "
				+ AsalKota
				+ "\n"
				+ "TUJUAN: "
				+ TujuanKota
				+ "\n"
				+ "\n"
				+ "[Harga: Rp."
				+ Harga
				+ "]\n"
				+ "*******************************\n"
				+ "      TERIMA KASIH       \n"
				+ "    Mobile Ticketing     \n"
				+ "   oleh Immersa Labs     \n"
				+ "           2013          \n"
				+ "*******************************\n\n"
				+ "-------------------------------\n";
		bxl.printText(formattedString, BixolonPrinter.ALIGNMENT_CENTER,
				0, 0, true);
		bxl.cutPaper(true);
		return false;
	}


	public List<String> getBtAddr() {
		return btAddr;
	}

	/***
	 * For LOW-LEVEL MODIFIER ONLY
	 * 
	 * @param btAddr
	 */
	public void _setBtAddr(String btAddrs) {
		btSelectedAddr = btAddrs;
	}

}
