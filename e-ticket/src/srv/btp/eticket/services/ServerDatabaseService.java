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

import srv.btp.eticket.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
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

        String url_select = URLService;
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

				//TODO: Tabel dibaca disini ya.
				String name = jObject.getString("name");
				String tab1_text = jObject.getString("tab1_text");
				int active = jObject.getInt("active");
			} //end: for

			this.progressDialog.dismiss();

		} catch (JSONException e) {

			Log.e("JSONException", "Error: " + e.toString());

		} //end: catch (JSONException e)

	} //end: protected void onPostExecute(Void v)
	
	

} //end: class MyAsyncTask extends AsyncTask<String, String, Void>