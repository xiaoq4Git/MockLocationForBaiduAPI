package com.codoon.location.demo;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.codoon.baidulocationdemo.R;
import com.codoon.location.service.MockService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MockActivity extends Activity {
	private Button bStart, bStop, bPause, bContinue;
	private EditText latEditText, lonEditText;
	private RadioGroup group;
	private MockService bindService;
	private Timer timer;
	private final static double FAST_SPEED_VALUE = 0.000035;
	private final static double NORMAL_SPEED_VALUE = 0.00002;
	private final static double SLOW_SPEED_VALUE = 0.000015;

	private void initView() {
		bStart = (Button) this.findViewById(R.id.start);
		bPause = (Button) this.findViewById(R.id.pause);
		bContinue = (Button) this.findViewById(R.id.con);
		bStop = (Button) this.findViewById(R.id.stop);
		latEditText = (EditText) this.findViewById(R.id.latEditText);
		lonEditText = (EditText) this.findViewById(R.id.lonEditText);

		timer = new Timer();
		group = (RadioGroup) this.findViewById(R.id.speedGroup);
		group.setOnCheckedChangeListener(new MyCheckChangeListener());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mock);
		Intent intent = new Intent(this, MockService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		initView();
	}

	public void OnStartClickListener(View view) {
		if (latEditText.getText().toString().length() > 3 && lonEditText.getText().toString().length() > 3) {
			bindService.setLatitude(Double.parseDouble(latEditText.getText().toString()));
			bindService.setLongitude(Double.parseDouble(lonEditText.getText().toString()));
		}
		switch (group.getCheckedRadioButtonId()) {
		case R.id.fast:
			bindService.setPaceValue(FAST_SPEED_VALUE);
			break;
		case R.id.normal:
			bindService.setPaceValue(NORMAL_SPEED_VALUE);
			break;
		case R.id.slow:
			bindService.setPaceValue(SLOW_SPEED_VALUE);
			break;
		case R.id.other:
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					Random rand = new Random();
					double unit = ((double) 4 - rand.nextFloat() * 3) / 100000;
					bindService.setPaceValue(unit);
				}
			}, 0, 3 * 60 * 1000);
			break;
		}
		bStart.setVisibility(android.view.View.INVISIBLE);
		bPause.setVisibility(android.view.View.VISIBLE);
		bContinue.setVisibility(android.view.View.INVISIBLE);
		bStop.setVisibility(android.view.View.VISIBLE);
		Toast.makeText(this, "¿ªÊ¼", Toast.LENGTH_SHORT).show();
		bindService.startMock();
		
		Intent home = new Intent(Intent.ACTION_MAIN);
		home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		home.addCategory(Intent.CATEGORY_HOME);
		startActivity(home);
	}

	public void onStopClickListener(View view) {
		bStart.setVisibility(android.view.View.VISIBLE);
		bPause.setVisibility(android.view.View.INVISIBLE);
		bContinue.setVisibility(android.view.View.INVISIBLE);
		bStop.setVisibility(android.view.View.INVISIBLE);
		Toast.makeText(this, "Í£Ö¹", Toast.LENGTH_SHORT).show();
		bindService.stopMock();
	}

	public void onPauseClickListener(View view) {
		bStart.setVisibility(android.view.View.INVISIBLE);
		bPause.setVisibility(android.view.View.INVISIBLE);
		bContinue.setVisibility(android.view.View.VISIBLE);
		bStop.setVisibility(android.view.View.VISIBLE);
		Toast.makeText(this, "ÔÝÍ£", Toast.LENGTH_SHORT).show();
		bindService.pauseMock();
	}

	public void onContinueClickListener(View view) {
		bStart.setVisibility(android.view.View.INVISIBLE);
		bPause.setVisibility(android.view.View.VISIBLE);
		bContinue.setVisibility(android.view.View.INVISIBLE);
		bStop.setVisibility(android.view.View.VISIBLE);
		Toast.makeText(this, "¼ÌÐø", Toast.LENGTH_SHORT).show();
		bindService.continueMock();
	}

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MockService.MyBinder binder = (MockService.MyBinder) service;
			bindService = binder.getMockService();
		}
	};

	private class MyCheckChangeListener implements OnCheckedChangeListener {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.fast:
				bindService.setPaceValue(FAST_SPEED_VALUE);
				break;
			case R.id.normal:
				bindService.setPaceValue(NORMAL_SPEED_VALUE);
				break;
			case R.id.slow:
				bindService.setPaceValue(SLOW_SPEED_VALUE);
				break;
			case R.id.other:
				timer.schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						Random rand = new Random();
						double unit = ((double) 4 - rand.nextFloat() * 3) / 100000;
						bindService.setPaceValue(unit);
					}
				}, 0, 3 * 60 * 1000);
				break;
			}
		}
	}
}
