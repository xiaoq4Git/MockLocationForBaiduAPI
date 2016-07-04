package com.codoon.location.demo;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codoon.baidulocationdemo.R;
import com.codoon.location.service.GpsReplayService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class GpsReplayActivity extends Activity {
	private Button gpsReplay;
	private EditText gpsFileName;
	private GpsReplayService bindGpsService;
	private static final String FILENAME = "/Gpsdata/mock/";

	private List<Double> latList = new ArrayList<Double>();
	private List<Double> lonList = new ArrayList<Double>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.replay);

		Intent intent = new Intent(GpsReplayActivity.this, GpsReplayService.class);
		bindService(intent, conn, Context.BIND_AUTO_CREATE);
		initView();
	}

	public void initView() {
		gpsFileName = (EditText) this.findViewById(R.id.et2);
		gpsReplay = (Button) this.findViewById(R.id.replay);
		gpsReplay.setOnClickListener(new MySetOnClickListener());

	}

	private ServiceConnection conn = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			GpsReplayService.MyBinder binder = (GpsReplayService.MyBinder) service;
			bindGpsService = binder.getService1();
		}
	};

	private class MySetOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					File file = new File(Environment.getExternalStorageDirectory(), FILENAME+gpsFileName.getText().toString());
					if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						try {
							FileInputStream inputStream = new FileInputStream(file);
							DataInputStream dataIO = new DataInputStream(inputStream);
							Pattern pat = Pattern.compile("lat=\"(.+)\".lon=\"(.+)\"");
							StringBuffer sBuffer = new StringBuffer();
							String strLine = null;
							while((strLine =  dataIO.readLine()) != null){
								Matcher m = pat.matcher(strLine);
								if (m.find()) {
									latList.add(Double.parseDouble(m.group(1)));
									lonList.add(Double.parseDouble(m.group(2)));
								}
							}
							bindGpsService.setLatSendList(latList);
							bindGpsService.setLonSendList(lonList);
							bindGpsService.startMockLocation();
							Toast.makeText(GpsReplayActivity.this, "读取GPS路线成功，开始回放..", Toast.LENGTH_LONG).show();
							
							Intent home = new Intent(Intent.ACTION_MAIN);
							home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							home.addCategory(Intent.CATEGORY_HOME);
							startActivity(home);
						} catch (Exception e) {
							Toast.makeText(GpsReplayActivity.this, "读取失败", Toast.LENGTH_SHORT).show();
						}
					} else {
						// 此时SDcard不存在或者不能进行读写操作的
						Toast.makeText(GpsReplayActivity.this, "此时SDcard不存在或者不能进行读写操作", Toast.LENGTH_SHORT).show();
					}
				}
			}).start();

		}
	}
}
