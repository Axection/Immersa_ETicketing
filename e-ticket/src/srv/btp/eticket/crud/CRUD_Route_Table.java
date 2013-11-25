package srv.btp.eticket.crud;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CRUD_Route_Table extends SQLiteOpenHelper {
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
	private static final String DATABASE_NAME = "route_table";
	private static final String TABLE_NAME = "route_forward"; //TABEL MAJU
	
	//entries
	private static final String KEY_ID = "id"; // nomor prioritas
	private static final String KEY_NAMA = "nama"; //nama kota
	private static final String KEY_LEFTPRICE = "leftprice"; //harga kiri
	private static final String KEY_RIGHTPRICE = "rightprice"; //harga kanan
	private static final String KEY_LOCATION = "lokasi"; //nama lokasi
	
	public CRUD_Route_Table(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public CRUD_Route_Table(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

		//
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String SQL_CREATION = "CREATE TABLE " + TABLE_NAME + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAMA + " TEXT," 
				+ KEY_LEFTPRICE + " INTEGER," + KEY_RIGHTPRICE + " INTEGER," + KEY_LOCATION + " TEXT"
				+ ")";
		db.execSQL(SQL_CREATION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
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
		val.put(KEY_LOCATION, t.get_lokasi());
		
		db.insert(TABLE_NAME, null, val);
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
				t.set_lokasi(c.getString(4));
				
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
				new String[] {KEY_ID, KEY_NAMA, KEY_LEFTPRICE, KEY_RIGHTPRICE, KEY_LOCATION},
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
				c.getString(4) //namalokasi lel
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
		val.put(KEY_LOCATION, t.get_lokasi());
		
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