package srv.btp.eticket.obj;

import srv.btp.eticket.R;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class Indicator {
	public ImageView img;
	private CharSequence label;
	public ImageView balloon;
	private int isEnabled = 0;
	public TextView txt;
	
	public Indicator(CharSequence lbl, Context targetContext){
		img = new ImageView(targetContext);
		
		txt = new TextView(targetContext);
		this.setLabel(lbl);
		txt.setText(lbl);
		
		balloon = new ImageView(targetContext);
		img.setImageResource(R.drawable.indicator_off);
		balloon.setImageResource(R.drawable.balloon);
		
		img.setScaleType(ScaleType.CENTER_CROP);
		balloon.setScaleType(ScaleType.CENTER_CROP);
		txt.setTextColor(targetContext.getResources().getColor(R.color.white));
		txt.setTextSize(22);
		txt.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD);
		txt.setGravity(1);
		
		img.setVisibility(View.VISIBLE);
		balloon.setVisibility(View.VISIBLE);
		txt.setVisibility(View.VISIBLE);
		
	}
	
	public void setLabel(CharSequence c){
		label = c;
		txt.setText(c);
	}
	
	public CharSequence getLabel(){
		return label;
	}
	
	public void setEnabled(int active) {
		if (active==0) {
			img.setImageResource(R.drawable.indicator_off);
			balloon.setImageResource(R.drawable.balloon);
		} else if(active==1) {
			img.setImageResource(R.drawable.indicator_on);
			balloon.setImageResource(R.drawable.balloon);
		} else {
			img.setImageResource(R.drawable.indicator_disabled);
			balloon.setImageResource(R.drawable.balloon_disabled);
		}
	}
	
	public int getEnabled(){
		return isEnabled;
	}
	
	

}
