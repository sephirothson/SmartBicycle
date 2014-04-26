package com.nit.smartbicycle;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class GuideActivity extends Activity {
	private View vBack = null;
	private View vBuka = null;
	private TextView vCon1 = null;
	private boolean bShow1 = false;
	private View vBanka = null;
	private TextView vCon2 = null;
	private boolean bShow2 = false;
	private View vShoufei = null;
	private TextView vCon3 = null;
	private boolean bShow3 = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_guide);

		vBack = (View) findViewById(R.id.guideback);
		vBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		ShowListener sl = new ShowListener();
		vBuka = (View) findViewById(R.id.buka);
		vCon1 = (TextView) findViewById(R.id.con1);
		vBuka.setOnClickListener(sl);

		vBanka = (View) findViewById(R.id.banka);
		vCon2 = (TextView) findViewById(R.id.con2);
		vBanka.setOnClickListener(sl);

		vShoufei = (View) findViewById(R.id.shoufei);
		vCon3 = (TextView) findViewById(R.id.con3);
		vShoufei.setOnClickListener(sl);

	}

	class ShowListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.buka:
				if (bShow1) {
					vCon1.setVisibility(View.GONE);
					bShow1 = false;
				} else {
					vCon1.setVisibility(View.VISIBLE);
					bShow1 = true;					
				}
				break;
			case R.id.banka:
				if (bShow2) {
					vCon2.setVisibility(View.GONE);
					bShow2 = false;
				} else {
					vCon2.setVisibility(View.VISIBLE);
					bShow2 = true;
				}
				break;
			case R.id.shoufei:
				if (bShow3) {
					vCon3.setVisibility(View.GONE);
					bShow3 = false;
				} else {
					vCon3.setVisibility(View.VISIBLE);
					bShow3 = true;					
				}
				break;
			}
		}
	}
}
