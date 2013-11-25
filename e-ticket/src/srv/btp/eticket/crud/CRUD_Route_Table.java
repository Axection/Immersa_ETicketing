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
	private static final String DATABASE_NAME = "queryLogger";
	private static final String TABLE_NAME = "log";
	
	//entries
	private static final String KEY_ID = "id";
	private static final String KEY_NAMA = "nama";
	private static final String KEY_QTY = "qty";
	private static final String KEY_GRADE = "grade";
	private static final String KEY_LOKASI = "lokasi";
	
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
				+ KEY_QTY + " INTEGER," + KEY_GRADE + " TEXT," + KEY_LOKASI + " TEXT"
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
		val.put(KEY_QTY, t.get_leftprice());
		val.put(KEY_GRADE, t.get_rightprice());
		val.put(KEY_LOKASI, t.get_lokasi());
		
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
				t.set_leftprice(c.getString(2));
				t.set_rightprice(c.getString(3));
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
				new String[] {KEY_ID, KEY_NAMA, KEY_QTY, KEY_GRADE, KEY_LOKASI},
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
				Integer.parseInt(c.getString(0)), 
				c.getString(1), 
				c.getString(2),
				c.getString(3),
				c.getString(4)
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
		val.put(KEY_QTY, t.get_leftprice());
		val.put(KEY_GRADE, t.get_rightprice());
		val.put(KEY_LOKASI, t.get_lokasi());
		
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
