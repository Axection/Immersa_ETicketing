package srv.btp.eticket;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginDialog extends Dialog implements
		android.view.View.OnClickListener {

	public Activity c;
	public Dialog d;
	public EditText username;
	public EditText password;


	public LoginDialog(Activity a) {
		super(a);
		this.c = a;
		this.setCanceledOnTouchOutside(false);
		this.setCancelable(false);
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sign);

		getWindow().setBackgroundDrawableResource(R.color.white);
		

		username = (EditText) findViewById(R.id.txtUsername);
		password = (EditText) findViewById(R.id.txtpassword);

		//Create Negative Positive
		
		
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_done:
			v.setBackgroundResource(R.drawable.button_dark);
			dismiss();
			break;
		default:
			break;
		}
		dismiss();
	}
	
	
}