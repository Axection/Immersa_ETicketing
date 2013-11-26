package srv.btp.eticket.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import srv.btp.eticket.crud.CRUD_Route_Back_Table;
import srv.btp.eticket.crud.CRUD_Route_Table;
import srv.btp.eticket.crud.Datafield_Route;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class ServerDatabaseService extends AsyncTask<String, String, Void> {
	/***
	 * ServerDatabaseService.java
	 * 
	 * Kelas ini dibuat untuk membaca data dari URL Service. 
	 * Pemanggilannya cukup mudah, cukup call syntax dan kemudian JSON 
	 * akan diambil.
	 * 
	 * Kelas ini akan mengambil dokumen daftar rute dan daftar rute balik.
	 * 
	 * Info menyusul
	 */
	//Variable Lists
	String URLService = PreferenceManager.getDefaultSharedPreferences(
			FormObjectTransfer.main_activity.getApplicationContext())
			.getString("service_address",
					FormObjectTransfer.main_activity.getResources().getString(
							R.string.default_service));
	
	
	private ProgressDialog progressDialog = new ProgressDialog(
			(Activity) FormObjectTransfer.main_activity);
	InputStream inputStream = null;
	ArrayList<String> result;
	CRUD_Route_Table route = new CRUD_Route_Table(FormObjectTransfer.main_activity.getApplicationContext());
	CRUD_Route_Back_Table route_back = new CRUD_Route_Back_Table(FormObjectTransfer.main_activity.getApplicationContext());
	
	boolean isReversed = false;
	boolean isVersionUptoDate = false;
	boolean isPricing = false;
	
	//FINALS
	public static final String FIELD_ID = "ID";
	public static final String FIELD_NAMA = "nama_kota";
	public static final String FIELD_LAT = "latitude";
	public static final String FIELD_LONG = "longitude";
	
	public static final String FIELD_ID_SRC = "id_lokasi_asal";
	public static final String FIELD_ID_DST = "id_lokasi_tujuan";
	public static final String FIELD_PRICE = "harga";
	
	
	public static final String URL_SERVICE_FORWARD = "lokasi";
	public static final String URL_SERVICE_REVERSE = "lokasi";
	
	public static final String URL_SERVICE_PRICE_FORWARD = "harga_lokasi_trayek";
	public static final String URL_SERVICE_PRICE_REVERSE = "harga_lokasi_trayek";
	public static final String URL_SERVICE_VERSION_CHECK = "/+/URL_CEK_VERSI_DB";
	
	public static final String URL_SERVICE_TRAJECTORY = "trayek";
	
	
	public static final int CHECK_STATE_VERSION = 1;
	public static final int CHECK_ROUTE_FORWARD = 2;
	public static final int CHECK_ROUTE_REVERSE = 3;
	public static final int CHECK_PRICE_FORWARD = 4;
	public static final int CHECK_PRICE_REVERSE = 5;
	public static final int CHECK_UNKNOWN = 0;
	
	public int connStatus = 0;
	public static boolean isDone;


	
	
	protected void onPreExecute() {
		isDone = false;
		progressDialog.setMessage("Mendownload beberapa informasi data...");
		progressDialog.show();
		progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				cancel(true);
			}
		});
	}

	@Override
    protected Void doInBackground(String ... params) {
		result = new ArrayList<String>();
		
		for(String parameter: params){
		String url_select = URLService + parameter;
        
        //STATE detection
        if(parameter.equals(URL_SERVICE_FORWARD)){
        	connStatus = CHECK_ROUTE_FORWARD;
        }else if(parameter.equals(URL_SERVICE_REVERSE)){ 
        	connStatus = CHECK_ROUTE_REVERSE;
        }else if(parameter.equals(URL_SERVICE_VERSION_CHECK)){
        	connStatus = CHECK_STATE_VERSION;
        	isVersionUptoDate = false; //Agar doInBackground wajib melakukan download versioning.
        }else if(parameter.equals(URL_SERVICE_PRICE_FORWARD)){
        	connStatus = CHECK_PRICE_FORWARD;
        }else if(parameter.equals(URL_SERVICE_PRICE_REVERSE)){
        	connStatus = CHECK_ROUTE_REVERSE;
        }
        
                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();
        if(!isVersionUptoDate){ //ketika versi telah terupdate, perlu adanya pencegahan download.
        try {
            // Set up HTTP post
            // HttpClient is more then less deprecated. Need to change to URLConnection
            HttpClient httpClient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url_select);
            httpPost.setEntity(new UrlEncodedFormEntity(param));
            HttpResponse httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();

            // Read content & Log
            inputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e1) {
            Log.e("UnsupportedEncodingException", e1.toString());
            e1.printStackTrace();
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        }
        // Convert response to string using String Builder
        try {
            BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "iso-8859-1"), 8);
            StringBuilder sBuilder = new StringBuilder();

            String line = null;
            while ((line = bReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            inputStream.close();
            
            result.add(sBuilder.toString());
            //result = ;

        } catch (Exception e) {
            Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
        }
        }//end: !isVersionUptoDate
		}//end: for
		return null;
    } //end: protected Void doInBackground(String... params)

	protected void onPostExecute(Void v) {
		// parse massive JSON data from result
		for(String resultset: result){
		try {
			JSONArray jArray = new JSONArray(resultset);
			
			for (int i = 0; i < jArray.length(); i++) {
				
				JSONObject jObject = jArray.getJSONObject(i);
				Log.d("proc",resultset);
				switch(connStatus){
				case CHECK_STATE_VERSION:
					//TODO:code ngolah cek versi, masukkan ke PreferenceManager
					int originValue = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
							FormObjectTransfer.main_activity.getApplicationContext())
							.getString("unique_key",
									"997")
						);
					int downloadedValue = jObject.getInt("version");
					if(downloadedValue>originValue){
						isVersionUptoDate = false;
					}else{
						isVersionUptoDate = true; //membuat update tidak terjadi.
					}
					break;
				/*
				 * Untuk CHECK PRICE dibutuhkan sesuatu pemrosesan lanjut agar data
				 * left price sama right pricenya masuk ke database yang benar, bukan
				 * membuat tabel baru.
				 */
				case CHECK_PRICE_FORWARD:
					if(isVersionUptoDate)break;
					int id_src_fwd = jObject.getInt(FIELD_ID_SRC);
					int id_dst_fwd = jObject.getInt(FIELD_ID_DST);
					int price_fwd = jObject.getInt(FIELD_PRICE);
					ContentValues c = new ContentValues();
					c.put(CRUD_Route_Table.KEY_LEFTPRICE, price_fwd/2); //insert ke id_dst
					route.getWritableDatabase().update(
							CRUD_Route_Table.TABLE_NAME, //nama tabel
							c, //konten yang diupdate
							CRUD_Route_Table.KEY_ID + " = ?", //where
							new String[]{ String.valueOf(id_dst_fwd) } //id=dst
							);
					c = new ContentValues();
					c.put(CRUD_Route_Table.KEY_RIGHTPRICE, price_fwd/2); //insert ke id src
					route.getWritableDatabase().update(
							CRUD_Route_Table.TABLE_NAME, //nama tabel
							c, //konten yang diupdate
							CRUD_Route_Table.KEY_ID + " = ?", //where
							new String[]{ String.valueOf(id_src_fwd) } //id=dst
							);
				case CHECK_PRICE_REVERSE:
					if(isVersionUptoDate)break;
					int id_src_rev = jObject.getInt(FIELD_ID_SRC);
					int id_dst_rev = jObject.getInt(FIELD_ID_DST);
					int price_rev = jObject.getInt(FIELD_PRICE);
					
					ContentValues cc = new ContentValues();
					cc.put(CRUD_Route_Back_Table.KEY_LEFTPRICE, price_rev/2); //insert ke id_dst
					route_back.getWritableDatabase().update(
							CRUD_Route_Back_Table.TABLE_NAME, //nama tabel
							cc, //konten yang diupdate
							CRUD_Route_Back_Table.KEY_ID + " = ?", //where
							new String[]{ String.valueOf(id_dst_rev) } //id=dst
							);
					cc = new ContentValues();
					cc.put(CRUD_Route_Back_Table.KEY_RIGHTPRICE, price_rev/2); //insert ke id src
					route_back.getWritableDatabase().update(
							CRUD_Route_Back_Table.TABLE_NAME, //nama tabel
							cc, //konten yang diupdate
							CRUD_Route_Back_Table.KEY_ID + " = ?", //where
							new String[]{ String.valueOf(id_src_rev) } //id=dst
							);
					
				case CHECK_ROUTE_FORWARD:
					if(isVersionUptoDate)break;
					int id_forward = jObject.getInt(FIELD_ID);
					String nama_forward = jObject.getString(FIELD_NAMA);
					double latd_forward = jObject.getDouble(FIELD_LAT);
					double longd_forward = jObject.getDouble(FIELD_LONG);
					Datafield_Route dr_forward = new Datafield_Route(id_forward, nama_forward, 0, 0,
							latd_forward, longd_forward);
					route.addEntry(dr_forward);
					break;
				case CHECK_ROUTE_REVERSE:
					if(isVersionUptoDate)break;
					int id_reverse = jObject.getInt(FIELD_ID);
					String nama_reverse = jObject.getString(FIELD_NAMA);
					double latd_reverse = jObject.getDouble(FIELD_LAT);
					double longd_reverse = jObject.getDouble(FIELD_LONG);
					Datafield_Route dr_reverse = new Datafield_Route(id_reverse, nama_reverse, 0, 0,
							latd_reverse, longd_reverse);
					route.addEntry(dr_reverse);
					break;
				}

			} // end: for

			this.progressDialog.dismiss();
			isDone = true;

		} catch (JSONException e) {

			Log.e("JSONException", "Error: " + e.toString());

		} //end: catch (JSONException e)
		
		}//end: for(String resultset:result){

	} //end: protected void onPostExecute(Void v)
	
	

} //end: class MyAsyncTask extends AsyncTask<String, String, Void>