package srv.btp.eticket.crud;

public class Datafield_Route {
	
	//properties
	long _id;
	private String _nama;
	private String _leftprice;
	private String _rightprice;
	private String _lokasi;
	
	public Datafield_Route(){
		
	}
	
	public Datafield_Route(long serializeNumber, String nama, String leftprice, String rightprice, String lokasi){
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

	public String get_leftprice() {
		return _leftprice;
	}

	public void set_leftprice(String leftprice) {
		this._leftprice = leftprice;
	}

	public String get_rightprice() {
		return _rightprice;
	}

	public void set_rightprice(String rightprice) {
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
	
}
