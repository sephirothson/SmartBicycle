package com.nit.smartbicycle;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TimerActivity extends Activity{
	private View vBack = null;
	private Button bRemind = null;
	private boolean stateClock = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer);
		
		vBack = (View) findViewById(R.id.timerback);
		vBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		bRemind = (Button) findViewById(R.id.remind);
		bRemind.setOnClickListener(new ButtonListener());
	}
	
	class ButtonListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			if (stateClock) {
				bRemind.setText("Ã·–—Œ“");
				stateClock = false;
			} else {
				bRemind.setText("Õ£÷π");
				stateClock = true;
			}
		}
	}
}
