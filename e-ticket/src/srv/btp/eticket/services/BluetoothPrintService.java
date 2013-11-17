package srv.btp.eticket.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.ListView;

import com.bixolon.android.library.*;
import com.bixolon.android.*;

/***
 * BluetoothPrintService.java
 * 
 * Kelas ini dibuat untuk melakukan sambungan, komunikasi serta manajemen dengan
 * printer Bluetooth yang ada. API disediakan di CD.
 * 
 * Skema pemakaian :
 * - Sebelumnya user sudah harus melakukan pairing dengan printernya
 * - Bluetooth Enable
 * - Lookup printer
 * - Idling system, tunggu sampai ada command print
 * - Looping tes Reconnect
 * 
 * Info menyusul.
 */
public class BluetoothPrintService {

	/***
	 * btAddr = Bluetooth Address. Nilai ini akan berubah secara otomatis
	 * setelah fungsi {@link FindPrinter} dieksekusi.
	 */
	private List<String> btAddr;
	private String btSelectedAddr;
	BluetoothAdapter mBluetoothAdapter;

	String dataPrint = "";
	int lastStatus = 0;

	BxlService bxl;
	private int REQUEST_ENABLE_BT;

	private Activity selected_activity;

	public BluetoothPrintService(Activity c) {
		bxl = new BxlService();
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		selected_activity = c;
	}

	/***
	 * Fungsi otomatis menemukan printer terdekat. Fungsi ini akan mengubah
	 * nilai {@link btAddr}
	 * 
	 * @return Nilai code hasil ditemukan atau tidak. (0= Sukses, 1= Gagal)
	 * 
	 */
	public int FindPrinter() {
		btAddr.clear();
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
		ArrayList<String> mArrayAdapter = new ArrayList<String>();
		// If there are paired devices
		if (pairedDevices.size() > 0) {
		    // Loop through paired devices
		    for (BluetoothDevice device : pairedDevices) {
		    	 mArrayAdapter.add(device.getName() + "|" + device.getAddress());
		        if(device.getBluetoothClass().getDeviceClass() == BluetoothClass.Device.Major.IMAGING){
		        	_setBtAddr(device.getAddress());
		        	return 0;
		        }
		    }
		}
		return 1;

	}

	public int ConnectPrinter() {
		boolean b = EnableBT();
		if (!b)return -1;
		int retVal = bxl.Connect(btSelectedAddr);
		return retVal;
	}

	public boolean EnableBT() {
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			selected_activity.startActivityForResult(enableBtIntent,
					REQUEST_ENABLE_BT);
		}
		if (REQUEST_ENABLE_BT == Activity.RESULT_OK) {
			return true;
		}
		return false;
	}

	public boolean PrintText(String ID, String AsalKota, String TujuanKota, int tiketNum, int Harga){
		//Buat persiapan terlebih dahulu untuk format
		String formattedString = "";
		/*
		 * Sebelumnya, ada sedikit penjelasan dengan format yang dipakai dalam print kali ini.
		 * ID = ID serialisasi dari tiket. Dapatkan serialisasi ID nanti dari kelas Serialisasi.
		 * AsalKota = Kota asal. Format String saja.
		 * TujuanKota = Kota tujuan.
		 * tiketNum = Jumlah tiket yang di print. Ingat, sistematikanya adalah multiprint.
		 * Artinya, Apabila dipesan 3, maka diprint tiga kali. bukan ditulis angka 3.
		 * Harga = Harga satuan dari per tiket. Tidak perlu print total.
		 */
		//TODO : Bikin format text
		
		return false;
	}
	
	public int ReconnectPrinter() {
		/*
		 * Return type : int
		 * 0 = Reconnect Sukses
		 * 1 = Reconnect Gagal
		 * -1 = Reconnect Tidak Diperlukan
		 */
		int res = bxl.GetStatus();
		if(res != BxlService.BXL_SUCCESS){
			//TODO : Eksekusi ulang Connect Printer
		}
		return 0;
	}

	public List<String> getBtAddr() {
		return btAddr;
	}

	/***
	 * For LOW-LEVEL MODIFIER ONLY
	 * @param btAddr
	 */
	public void _setBtAddr(String btAddrs) {
		btAddr.add(btAddrs);
		btSelectedAddr = btAddrs;
	}

}
