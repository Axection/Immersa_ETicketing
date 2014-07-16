/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package srv.btp.eticket.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import srv.btp.eticket.services.TermKeyListener;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bixolon.printer.BixolonPrinter;



/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothSerialService {
    // Debugging
    private static final String TAG = "BluetoothReadService";
    private static final boolean D = true;
 // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    
	public static final int ORIENTATION_SENSOR    = 0;
	public static final int ORIENTATION_PORTRAIT  = 1;
	public static final int ORIENTATION_LANDSCAPE = 2;

	// Message types sent from the BluetoothReadService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;	

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    
    private boolean mAllowInsecureConnections = true;
    private int mIncomingEoL_0D = 0x0D;
    private int mIncomingEoL_0A = 0x0A;
    private int mOutgoingEoL_0D = 0x0D;
    private int mOutgoingEoL_0A = 0x0A;
    
    public boolean READY_STATE = false;
    
    public CountDownTimer sharedCountdown;
	
	private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
	public String btSelectedAddr;
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    
    
    private Context mContext;
	private ArrayList<String> btAddr;
	public ImageView BTIndicator;
	private Activity selected_activity;
	private TermKeyListener mKeyListener;
	
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

	public static final int DEFAULT_RECONNECT_FAIL_TIME = 10000;
	public static final int DEFAULT_RECONNECT_SUCCESS_TIME = 10000;
	protected static final boolean DEBUG = false;
    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothSerialService(Activity c, ImageView Indicators) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = BLUETOOTH_HANDLER;
        selected_activity = c;
        mContext = c.getApplicationContext();
        mAllowInsecureConnections = true;
        btAddr = new ArrayList<String>();
		BTIndicator = Indicators;
		RecreateTimer(DEFAULT_RECONNECT_FAIL_TIME);
		//DisconnectPrinter();
		READY_STATE = true;
		register(mKeyListener);
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState() {
        return mState;
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");


        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        setState(STATE_NONE);
    }
    
    /***
	 * For LOW-LEVEL MODIFIER ONLY
	 * 
	 * @param btAddr
	 */
	public void _setBtAddr(String btAddrs) {
		btSelectedAddr = btAddrs;
	}
    
    public int FindPrinters() {
		btAddr.clear();
		Set<BluetoothDevice> pairedDevices = mAdapter
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
    
    public boolean ConnectPrinter() {
    	boolean b = EnableBT();
		if (!b)
			return b;
		Log.d("ConnectPrinter","State=" + getState());
		if(getState() != STATE_CONNECTED && getState() != STATE_CONNECTING){
			Log.e("BTAddress", btSelectedAddr);
			BluetoothDevice device = mAdapter.getRemoteDevice(btSelectedAddr);
			// Attempt to connect to the device
			connect(device);            
		}
		return b;
	}
	
	public void DisconnectPrinter(){
		stop();
	}
    
    public void RecreateTimer(int RECONNECT_TIMEOUT){
		sharedCountdown = new CountDownTimer(RECONNECT_TIMEOUT,RECONNECT_TIMEOUT/10){
			@Override
			public void onFinish() {
				if(!FormObjectTransfer.isQuit /*&& BT_STATE != STATE_CONNECTED*/ )
				Log.d(this.getClass().toString(),"DONE!");
				// ganti ConnectPrinter(); menjadi metode connect native
				ConnectPrinter();
			}
			@Override public void onTick(long millisUntilFinished) {
				Log.d(this.getClass().toString(),millisUntilFinished + " " + "countdown to reconnect");
			}
		};
	}
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, ("Sambungan printer terputus.\n Menyambung kembali dalam waktu " + (DEFAULT_RECONNECT_FAIL_TIME/1000) +" detik...") );
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Printer bluetooth terputus..." );
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }
    
    public boolean EnableBT() {
		if (!mAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			selected_activity.startActivityForResult(enableBtIntent,
					REQUEST_ENABLE_BT);
		} else {
			return true;
		}
		return false;
	}
    
    
    //Precommunication PrintText
    public void register(TermKeyListener listener) {
        mKeyListener = listener;
    }
    
    private void sendText(CharSequence text) {
        CharSequence tmpText = text;
        byte[] dataPost = ((String) tmpText).getBytes(Charset.forName("UTF-8"));
    	/*int n = text.length();
        try {
        	mapAndSend(0x1B);
        	mapAndSend(0x61);
            for(int i = 0; i < n; i++) {
                char c = text.charAt(i);
                mapAndSend(c);
            }
        } catch (IOException e) {
        }*/
        try{
        	mapAndSend(0x1B);
        	mapAndSend(0x61);
        	mapAndSend(0x01);
        	send(dataPost);
        }catch(Exception e){
        	e.printStackTrace();
        }
    }

    private void mapAndSend(int c) throws IOException {
    	byte[] mBuffer = new byte[1];
    	mBuffer[0] = (byte)c;
    	
    	send(mBuffer);
    }
    
    private byte[] handleEndOfLineChars( int outgoingEoL ) {
		byte[] out;
		
	    if ( outgoingEoL == 0x0D0A ) {
	    	out = new byte[2];
	    	out[0] = 0x0D;
	    	out[1] = 0x0A;
		}
	    else {
		    if ( outgoingEoL == 0x00 ) {
		    	out = new byte[0];
		    }
		    else {
		    	out = new byte[1];
		    	out[0] = (byte)outgoingEoL;
		    }
	    }
		
		return out;
	}

    
    public void send(byte[] out) {
    	
    	/*if ( out.length == 1 ) {
    		
    		if ( out[0] == 0x0D ) {
    			out = handleEndOfLineChars( mOutgoingEoL_0D );
    		}
    		else {
        		if ( out[0] == 0x0A ) {
        			out = handleEndOfLineChars( mOutgoingEoL_0A );
        		}
    		}
    	}*/
    	
    	if ( out.length > 0 ) {
    		write( out );
    	}
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
		 * ************************* 
		 * ****MOBILE TICKETING*****
		 * ************************* 
		 * <hari>, <tgl> Waktu : <jam> 
		 * NOMOR TIKET:<ID>
		 * 
		 * DETAIL TIKET ANDA 
		 * ASAL : <asal> 
		 * TUJUAN <tujuan>
		 * 
		 * [<harga>]
		 * 
		 * ************************* 
		 * TERIMA KASIH 
		 * Mobile Ticketing 
		 * oleh Immersa Labs 2013 
		 * *************************
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
		
		//TODO: Disini perlu ada pergantian sistem ngeprint. dimana ngeprint sekarang perlu kejut2 :v
		/*
		 * 
		bxl.printText(formattedString, BixolonPrinter.ALIGNMENT_CENTER,
				0, 0, true);
		bxl.cutPaper(true);
		*/
		sendText(formattedString);
		return false;
	}
    
    /**
     * 
     * Untuk berjaga2. Handler dari BPS dibawa kesini...
     * 
     */
    
private final Handler BLUETOOTH_HANDLER = new Handler(new Handler.Callback() {
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case BixolonPrinter.MESSAGE_STATE_CHANGE:
				Log.d("BTMessage",msg.arg1+ "");
				switch (msg.arg1) {
				case BluetoothSerialService.STATE_CONNECTING://BixolonPrinter.STATE_CONNECTING:
					Toast.makeText(selected_activity.getApplicationContext(), "Menyambung ke printer bluetooth...", Toast.LENGTH_SHORT).show();	
					BTIndicator.setImageResource(R.drawable.indicator_bt_warn);
					FormObjectTransfer.isBTConnected = false;
					FormObjectTransfer.main_activity.checkStatus();
					mState = STATE_CONNECTING;
					break;
				case BluetoothSerialService.STATE_CONNECTED://BixolonPrinter.STATE_CONNECTED:
					Toast.makeText(selected_activity.getApplicationContext(), "Sambungan ke Bluetooth berhasil.", Toast.LENGTH_SHORT).show();
					mState = STATE_CONNECTED;
					BTIndicator.setImageResource(R.drawable.indicator_bt_on);
					FormObjectTransfer.isBTConnected = true;
					FormObjectTransfer.main_activity.checkStatus();
			        sharedCountdown.cancel(); //Untuk menetapkan overriding reconnect manual.
			        RecreateTimer(DEFAULT_RECONNECT_SUCCESS_TIME);
					sharedCountdown.start();
					break;
				case BluetoothSerialService.STATE_NONE://BixolonPrinter.STATE_NONE:
					//DisconnectPrinter();
					//if(BT_STATE == STATE_CONNECTED)break;
			        /*Toast.makeText(selected_activity.getApplicationContext(), 
			        			,
			        			Toast.LENGTH_LONG)
			        			.show();*/
			        
					BTIndicator.setImageResource(R.drawable.indicator_bt_off);
					mState = STATE_LISTEN;
					if(READY_STATE){
						FormObjectTransfer.isBTConnected = false;
						//if(!FormObjectTransfer.isInitalizationState)
						FormObjectTransfer.main_activity.checkStatus();
				        sharedCountdown.cancel(); //Untuk menetapkan overriding reconnect manual.
				        RecreateTimer(DEFAULT_RECONNECT_FAIL_TIME);
						sharedCountdown.start();
					}
					break;
				}
				break;
			case BixolonPrinter.MESSAGE_DEVICE_NAME:
				@SuppressWarnings("unused")
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
    
	// The Handler that gets information back from the BluetoothService
	
private final Handler mHandlerBT = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (DEBUG)
					Log.i("BluetoothSerialService", "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothSerialService.STATE_CONNECTED:
					/*if (mMenuItemConnect != null) {
						mMenuItemConnect
								.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
						mMenuItemConnect.setTitle(R.string.disconnect);
					}

					mInputManager.showSoftInput(mEmulatorView,
							InputMethodManager.SHOW_IMPLICIT);

					mTitle.setText(R.string.title_connected_to);
					mTitle.append(" " + mConnectedDeviceName);*/
					break;

				case BluetoothSerialService.STATE_CONNECTING:
					//mTitle.setText(R.string.title_connecting);
					break;

				case BluetoothSerialService.STATE_LISTEN:
				case BluetoothSerialService.STATE_NONE:
					/*if (mMenuItemConnect != null) {
						mMenuItemConnect
								.setIcon(android.R.drawable.ic_menu_search);
						mMenuItemConnect.setTitle(R.string.connect);
					}

					mInputManager.hideSoftInputFromWindow(
							mEmulatorView.getWindowToken(), 0);

					mTitle.setText(R.string.title_not_connected);
					 */
					break;
				}
				break;
			case MESSAGE_WRITE:
				/*if (mLocalEcho) {
					//byte[] writeBuf = (byte[]) msg.obj;
					///mEmulatorView.write(writeBuf, msg.arg1);
				}*/

				break;
			case MESSAGE_DEVICE_NAME:
				/*// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(
						getApplicationContext(),
						getString(R.string.toast_connected_to) + " "
								+ mConnectedDeviceName, Toast.LENGTH_SHORT)
						.show();*/
				break;
			case MESSAGE_TOAST:
				Toast.makeText(mContext,
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	/**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
            	if ( mAllowInsecureConnections ) {
            		Method method;
            		
            		method = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class } );
                    tmp = (BluetoothSocket) method.invoke(device, 1);  
            	}
            	else {
            		tmp = device.createRfcommSocketToServiceRecord( SerialPortServiceClass_UUID );
            	}
            } catch (Exception e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                //BluetoothSerialService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSerialService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    //mEmulatorView.write(buffer, bytes);
                    // Send the obtained bytes to the UI Activity
                    //mHandler.obtainMessage(BlueTerm.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(MESSAGE_WRITE, buffer.length, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
    
    public void setAllowInsecureConnections( boolean allowInsecureConnections ) {
    	mAllowInsecureConnections = allowInsecureConnections;
    }
    
    public boolean getAllowInsecureConnections() {
    	return mAllowInsecureConnections;
    }
    
	

}
