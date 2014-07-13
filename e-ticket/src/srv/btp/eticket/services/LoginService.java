package srv.btp.eticket.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import srv.btp.eticket.FormObjectTransfer;
import srv.btp.eticket.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.Log;

public class LoginService extends AsyncTask<String, Integer, Boolean> {

	// Ada kabar gembira untuk kita semua :v
	// TODO:Gabungan SubmitData dan Retrieve Data

	public static final String FIELD_TABLE_NAME = "user"; // Nama tabel login
	public static final int MAXIMUM_WAITING_TIME = 60000;
	public static final String TABLE_ID = "id";
	public static final String TABLE_USERNAME = "username";
	// General
	static int respondCode = 0;
	public static boolean isDone;
	public static boolean isFail = false;

	private boolean isGetDataFailed;
	String URLService = PreferenceManager.getDefaultSharedPreferences(
			FormObjectTransfer.main_activity.getApplicationContext())
			.getString(
					"service_address",
					FormObjectTransfer.main_activity.getResources()
							.getString(R.string.default_service));
	private ProgressDialog progressDialog = new ProgressDialog(
			(Activity) FormObjectTransfer.current_activity);
	
	public static String message = "Menyambung akun...";
	protected CountDownTimer ctd = new CountDownTimer(MAXIMUM_WAITING_TIME, 200) {
		@Override
		public void onTick(long arg0) {
			Log.d("RouteTickingWaiting", arg0 + " JSON waiting time.");
			progressDialog.setMessage(message
					+ "\nTekan tombol 'Back' untuk batal.");
		}
		@Override
		public void onFinish() {
			Log.e("ctd","Forced Exit");
			progressDialog.cancel();
		}
	};

	// Submission Type
	String username;
	String password;

	// Retrieving Type
	int userID = -1;
	String userNameResult = "";
	InputStream inputStream = null;
	String result;

	@Override
	protected void onPreExecute() {
		isDone = false;

		// Membersihkan FormObjectTransfer
		progressDialog.setMessage(message);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setCancelable(true);
		progressDialog.show();
		ctd.start();

		progressDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				progressDialog.dismiss();
				if(!isDone){
					isFail = true;
				}
				isDone = false;
				cancel(true);
				ctd.cancel();

			}
		});
	}
	
	@Override
	protected Boolean doInBackground(String... values) {
		if (values.length == 2){
			Log.v("LoginService : status", "Valid data! Working...");
			// 0 : username
			if (values[0] != null) {
				username = values[0];
			}
			// 1 : passowrd
			if (values[1] != null) {
				password = values[1];
			}

			int status = postData(username, password);
			
			Log.v("LoginService : status", status + "");
			LoginService.respondCode = userID;
			if (status == 200){
				FormObjectTransfer.UserID = userID;
				FormObjectTransfer.UserName = userNameResult;
				//Writing to Preferences
				Log.d("LoginService status 200", "writing preferences data : " + userID + " " + userNameResult);
				PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.current_activity.getApplicationContext())
						.edit()
						.putInt("user_id", userID)
						.commit();
				
				PreferenceManager.getDefaultSharedPreferences(
						FormObjectTransfer.current_activity.getApplicationContext())
						.edit()
						.putString("username", userNameResult)
						.commit();
				return true;
			}
			else
				
				return false;
		}else
		{
			return false;
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	};

	@Override
	protected void onPostExecute(Boolean result) {
		ctd.cancel();
		this.progressDialog.dismiss();
		isDone = true;
		if(isGetDataFailed) 
			isFail = true;
		else
			isFail = false;
	};

	private int postData(String username, String password) {
		String target_post = URLService 
				+ FormObjectTransfer.main_activity.getResources().getString(R.string.extension_service)
				+ FIELD_TABLE_NAME;
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(target_post);
		int code = -1;
		List<NameValuePair> nameValuePairs = null;
		try {
			nameValuePairs = new ArrayList<NameValuePair>(2);
			// TODO:username & password pairs
			nameValuePairs.add(new BasicNameValuePair("username", username));
			nameValuePairs.add(new BasicNameValuePair("password", password));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			Log.v("LoginService : target", target_post);
			Log.d("LoginService : nameValuePairs", Arrays.toString(nameValuePairs.toArray()) );

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			//waiting for da respond
			HttpEntity entity = response.getEntity();
			//String responseString = EntityUtils.toString(entity, "iso-8859-1");
			//Log.v("LoginService : response string", responseString);
			getEntityContent(entity);
			code = response.getStatusLine().getStatusCode();
			return code;
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.toString());
			e.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			
		}catch (UnsupportedEncodingException e1) {
			Log.e("UnsupportedEncodingException", e1.toString());
			e1.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
		} catch (IllegalStateException e3) {
			Log.e("IllegalStateException", e3.toString());
			e3.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			return -1024;
		} catch (UnknownHostException e4) {
			Log.e("UnknownHost", e4.toString());
			e4.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			return -1024;
		}
		catch (IOException e) {
			Log.d("error LoginService query io exception", e.getMessage());
			Log.d("error LoginService server URL", target_post);
			Log.e("IOException", e.toString());
			e.printStackTrace();
			isFail = true;
			isDone = false;
			isGetDataFailed = true;
			return -8;
		}
		return code;
	}

	private void getEntityContent(HttpEntity httpEntity){
			try {
				inputStream = httpEntity.getContent();
			} catch (IllegalStateException e) {
				Log.e("IllegalStateException", e.toString());
				e.printStackTrace();
				isFail = true;
				isDone = false;
			} catch (IOException e) {
				Log.e("IOException", e.toString());
				e.printStackTrace();
				isFail = true;
				isDone = false;
			}
			
			// Convert response to string using String Builder
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(inputStream, "iso-8859-1"),
						65728);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					Log.d("BufferingData", "lineworks = " + line);
					sb.append(line + " ");
				}
				inputStream.close();
				Log.d("BufferingData", "Data= " + sb.toString());
				result = sb.toString();

			} catch (Exception e) {
				Log.e("StringBuilding & BufferedReader",
						"Error converting result " + e.toString());
				isGetDataFailed = true;
			}
			//Mengolah result userID
			try {
				//JSONArray jArray = new JSONArray(result);	
				JSONObject jObject = (JSONObject) new JSONTokener(result).nextValue();//jArray.getJSONObject(0);
				Log.d("parse",jObject.toString());
				userID = jObject.getInt(TABLE_ID);
				userNameResult = jObject.getString(TABLE_USERNAME);
				//Urgency alert
				if(userID < 0){
					isFail = true;
					isGetDataFailed = true;
				}
			} 
			catch (JSONException e) {
				isFail = true;
				isDone = false;
				Log.e("JSONException", "Error: " + e.toString());
				isGetDataFailed = true;
				e.printStackTrace();
			} // end: catch (JSONException e)
			catch (ClassCastException castE){
				isFail = true;
				isDone = false;
				Log.e("ClassCasting", "Error: " + castE.toString());
				isGetDataFailed = true;
				userID = -1024;
				castE.printStackTrace();
			}
	}

}
