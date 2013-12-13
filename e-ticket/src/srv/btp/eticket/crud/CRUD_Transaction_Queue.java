package srv.btp.eticket.crud;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CRUD_Transaction_Queue extends SQLiteOpenHelper {
	/***
	 * DatabaseVersioning.java
	 * 
	 * Kelas ini dibuat untuk melakukan versioning check pada semua SQLite
	 * yang ada di aplikasi, menyangkut info lokasi kota, pricing, dan lainnya.
	 * 
	 * 
	 * Info menyusul
	 */


	private static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "route_table";
	public static final String TABLE_NAME = "transaction";
	public static final String TEMP_TABLE = "temp_transaction";
	
	//entries
	public static final String KEY_ID = "id"; // nomor prioritas
	public static final String KEY_NAMA = "nama"; //nama kota
	public static final String KEY_LEFTPRICE = "leftprice"; //harga kiri
	public static final String KEY_RIGHTPRICE = "rightprice"; //harga kanan
	public static final String KEY_LATITUDE = "latitude"; //nomor latitude
	public static final String KEY_LONGITUDE = "longitude"; //nomor longitude
	
	private static final String ID_TRAYEK = "ID_trayek"; //id trayek
	private static final String KOTA_1 = "kota1"; //id kota asal
	private static final String KOTA_2 = "kota2"; //id kota tujuan
	private static final String LONGITUDE = "long"; //id kota tujuan
	private static final String LATITUDE = "lat"; //id kota tujuan
	private static final String JUMLAH_TIKET = "tiket"; //id kota tujuan
	private static final String DATE_TIME = "date_transaction"; //id kota tujuan
	
	
	public CRUD_Transaction_Queue(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public CRUD_Transaction_Queue(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String SQL_CREATION = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID + " INTEGER," + KEY_NAMA + " TEXT," 
				+ KEY_LEFTPRICE + " INTEGER," + KEY_RIGHTPRICE + " INTEGER," 
				+ KEY_LATITUDE + " NUMBER,"
				+ KEY_LONGITUDE + " NUMBER"
				+ ")";				
		db.execSQL(SQL_CREATION);
		
		//create temp table for queueing
		SQL_CREATION = "CREATE TABLE " + TEMP_TABLE + "("
				+ ID_TRAYEK + " INTEGER," + KOTA_1 + " NUMBER," 
				+ KOTA_2 + " NUMBER," + LONGITUDE + " REAL," 
				+ LATITUDE + " REAL,"
				+ JUMLAH_TIKET + " NUMBER, " + DATE_TIME + " TEXT"
				+ ")";
		Log.v("Query ", SQL_CREATION);
		db.execSQL(SQL_CREATION);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TEMP_TABLE);
		onCreate(db);
	}
	
	public void addEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Add new entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(KEY_ID, t.get_ID());
		val.put(KEY_NAMA,t.get_nama());
		val.put(KEY_LEFTPRICE, t.get_leftprice());
		val.put(KEY_RIGHTPRICE, t.get_rightprice());
		val.put(KEY_LATITUDE, t.get_latitude());
		val.put(KEY_LONGITUDE, t.get_longitude());
		
		db.insert(TABLE_NAME, null, val);
		db.close();
	}
	
	public void addTempTransaction(List<NameValuePair> nameValuePairs){
		/**
		 * TEMP_TABLE + "("
				+ ID_TRAYEK + " INTEGER," + KOTA_1 + " NUMBER," 
				+ KOTA_2 + " NUMBER," + LONGITUDE + " REAL," 
				+ LATITUDE + " REAL,"
				+ JUMLAH_TIKET + " NUMBER, " + DATE_TIME + " TEXT"
		 */
		Log.d("SQLiteCRUD","Add new entry of temp transaction");
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(ID_TRAYEK, ((NameValuePair)nameValuePairs.get(0)).getValue());
		val.put(KOTA_1,((NameValuePair)nameValuePairs.get(1)).getValue());
		val.put(KOTA_2, ((NameValuePair)nameValuePairs.get(2)).getValue());
		val.put(LONGITUDE, ((NameValuePair)nameValuePairs.get(3)).getValue());
		val.put(LATITUDE, ((NameValuePair)nameValuePairs.get(4)).getValue());
		val.put(JUMLAH_TIKET, ((NameValuePair)nameValuePairs.get(5)).getValue());
		val.put(DATE_TIME, ((NameValuePair)nameValuePairs.get(6)).getValue());
		
		db.insert(TEMP_TABLE, null, val);
		db.close();
	}
	
	public List<Datafield_Route> getAllEntries() {
		Log.d("SQLiteCRUD","Get All entries");
		List<Datafield_Route> l = new ArrayList<Datafield_Route>();
		String query = "SELECT * FROM " + TABLE_NAME;
		
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor c = db.rawQuery(query, null);
		
		//proceeed
		if(c.moveToFirst()){
			do{
				Datafield_Route t = new Datafield_Route();
				t.set_ID(Integer.parseInt(c.getString(0)));
				t.set_nama(c.getString(1));
				t.set_leftprice(Integer.parseInt(c.getString(2)));
				t.set_rightprice(Integer.parseInt(c.getString(3)));
				t.set_latitude(Double.parseDouble(c.getString(4)));
				t.set_longitude(Double.parseDouble(c.getString(5)));
				
				l.add(t);
			} while (c.moveToNext());
		}
		
		return l;
	}
	
	public Datafield_Route getEntry(int id){
		Log.d("SQLiteCRUD","Lookup entry from " + id);
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.query(
				TABLE_NAME, 
				new String[] {KEY_ID, KEY_NAMA, KEY_LEFTPRICE, KEY_RIGHTPRICE, KEY_LATITUDE, KEY_LONGITUDE},
				KEY_ID + "=?", 
				new String[] {String.valueOf(id) },
				null,
				null,
				null, 
				null
				);
		
		if(c!= null){
			c.moveToFirst();
		}
		
		Datafield_Route t = new Datafield_Route(
				Integer.parseInt(c.getString(0)), //ID
				c.getString(1), //Nama
				Integer.parseInt(c.getString(2)), //leftprice
				Integer.parseInt(c.getString(3)), //rightprice
				Double.parseDouble(c.getString(4)),
				Double.parseDouble(c.getString(5))
				);
		return t;
	}

	public int countEntries(){
		Log.d("SQLiteCRUD","Counting Entries...");
		String query = "SELECT * FROM " + TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(query, null);
		c.close();
		
		return c.getCount();
	}
	
	public int updateEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Update Entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues val = new ContentValues();
		val.put(KEY_ID, t.get_ID());
		val.put(KEY_NAMA,t.get_nama());
		val.put(KEY_LEFTPRICE, t.get_leftprice());
		val.put(KEY_RIGHTPRICE, t.get_rightprice());
		val.put(KEY_LATITUDE, t.get_latitude());
		val.put(KEY_LONGITUDE, t.get_longitude());
		
		
		return db.update(TABLE_NAME, val, KEY_ID + " = ?",
				new String[]{ String.valueOf(t.get_ID() ) }
		);
	}
	
	public void deleteEntry(Datafield_Route t){
		Log.d("SQLiteCRUD","Delete entry of "+ t.toString());
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_NAME, KEY_ID + " = ?", 
				new String[] { String.valueOf(t.get_ID())
				});
		db.close();
		}
	
}

