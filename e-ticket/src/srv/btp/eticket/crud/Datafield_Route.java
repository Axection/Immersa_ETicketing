package srv.btp.eticket.crud;

public class Datafield_Route {
	
	//properties
	long _id;
	private String _nama;
	private int _leftprice;
	private int _rightprice;
	private String _lokasi;
	private long _latitude;
	private long _longitude;
	
	public Datafield_Route(){
		
	}
	
	public Datafield_Route(long serializeNumber, String nama, int leftprice, int rightprice, String lokasi){
		this._id = serializeNumber;
		this.set_nama(nama);
		this.set_leftprice(leftprice);
		this.set_rightprice(rightprice);
		this.set_lokasi(lokasi);
	}
	
	public long get_ID(){
		return _id;
	}
	
	public void set_ID(long _id){
		this._id  = _id;
	}

	public int get_leftprice() {
		return _leftprice;
	}

	public void set_leftprice(int leftprice) {
		this._leftprice = leftprice;
	}

	public int get_rightprice() {
		return _rightprice;
	}

	public void set_rightprice(int rightprice) {
		this._rightprice = rightprice;
	}

	public String get_nama() {
		return _nama;
	}

	public void set_nama(String _nama) {
		this._nama = _nama;
	}

	public String get_lokasi() {
		return _lokasi;
	}

	public void set_lokasi(String _lokasi) {
		this._lokasi = _lokasi;
	}

	public long get_latitude() {
		return _latitude;
	}

	public void set_latitude(long _latitude) {
		this._latitude = _latitude;
	}

	public long get_longitude() {
		return _longitude;
	}

	public void set_longitude(long _longitude) {
		this._longitude = _longitude;
	}
	
}
