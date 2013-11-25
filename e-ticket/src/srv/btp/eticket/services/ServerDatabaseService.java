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
	String result = "";
	CRUD_Route_Table route = new CRUD_Route_Table(FormObjectTransfer.main_activity.getApplicationContext());
	CRUD_Route_Back_Table route_back = new CRUD_Route_Back_Table(FormObjectTransfer.main_activity.getApplicationContext());
	
	boolean isReversed = false;
	boolean isVersionChecking = true;
	//FINALS
	public static final String FIELD_ID = "id";
	public static final String FIELD_NAMA = "nama";
	public static final String FIELD_LAT = "latitude";
	public static final String FIELD_LONG = "longitude";
	
	public static final String URL_SERVICE_FORWARD = "/+/URL_SUB_SERVICE_FORWARD_HERE";
	public static final String URL_SERVICE_REVERSE = "/+/URL_SUB_SERVICE_REVERSE_HERE";
	public static final String URL_SERVICE_VERSION_CHECK = "/+/URL_CEK_VERSI_DB";
	
	
	protected void onPreExecute() {
		progressDialog.setMessage("Processing jsOn Download..");
		progressDialog.show();
		progressDialog.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				cancel(true);
			}
		});
	}

	@Override
    protected Void doInBackground(String ... params) {

        String url_select = URLService + params[0];
        
        if(params[0].equals(URL_SERVICE_FORWARD)){ 
        	isReversed = false;
        }else if(params[0].equals(URL_SERVICE_REVERSE)){ 
        	isReversed = true;
        }else if(params[0].equals(URL_SERVICE_VERSION_CHECK)){
        	isVersionChecking = true;
        }
                ArrayList<NameValuePair> param = new ArrayList<NameValuePair>();

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
            result = sBuilder.toString();

        } catch (Exception e) {
            Log.e("StringBuilding & BufferedReader", "Error converting result " + e.toString());
        }
		return null;
    } //end: protected Void doInBackground(String... params)

	protected void onPostExecute(Void v) {
		// parse JSON data
		try {
			JSONArray jArray = new JSONArray(result);
			
			for (int i = 0; i < jArray.length(); i++) {

				JSONObject jObject = jArray.getJSONObject(i);

				if (isVersionChecking) {
					//TODO:code ngolah cek versi, masukkan ke PreferenceManager
					int originValue = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
							FormObjectTransfer.main_activity.getApplicationContext())
							.getString("unique_key",
									"997")
						);
					int downloadedValue = jObject.getInt("version");
					if(downloadedValue>originValue){
						//TODO:Lakukan perbolehkan perlakuan update.
						isVersionChecking = false;
					}else{
						isVersionChecking = true; //membuat update tidak terjadi.
					}
					
				} else {
					// TODO: Tabel dibaca disini ya.
					int id = jObject.getInt(FIELD_ID);
					String nama = jObject.getString(FIELD_NAMA);
					double latd = jObject.getDouble(FIELD_LAT);
					double longd = jObject.getDouble(FIELD_LONG);
					Datafield_Route dr = new Datafield_Route(id, nama, 0, 0,
							latd, longd);
					if (!isReversed) {
						route.addEntry(dr);
					} else {
						route_back.addEntry(dr);
					}
				}

			} // end: for

			this.progressDialog.dismiss();

		} catch (JSONException e) {

			Log.e("JSONException", "Error: " + e.toString());

		} //end: catch (JSONException e)

	} //end: protected void onPostExecute(Void v)
	
	

} //end: class MyAsyncTask extends AsyncTask<String, String, Void>