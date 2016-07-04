package com.codoon.location.service;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;

import com.codoon.baidulocationdemo.R;
import com.codoon.location.demo.GpsReplayActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class GpsReplayService extends Service {
	public static final String TAG = GpsReplayService.class.getName();
	private static final int UPDATE_TIME = 2000;
	private MyBinder myBinder = new MyBinder();
	private Handler handler = new Handler();
	private LocationManager mLocationManager;
	private static double LATITUDE = 0;
	private static double LONGITUDE = 0;
	private static List<Double> latSendList = new ArrayList<Double>();
	private static List<Double> lonSendList = new ArrayList<Double>();
	private int count = 0;
	private Random rad = new Random();
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}

	public void setLatSendList(List<Double> latList) {
		latSendList = latList;
	}

	public void setLonSendList(List<Double> lonList) {
		lonSendList = lonList;
	}

	public GpsReplayService() {
		Log.i(TAG, "服务初始化");
		File file = new File(Environment.getExternalStorageDirectory(), "/Gpsdata");
		if(!file.exists()){
			File file1 = new File(Environment.getExternalStorageDirectory(), "/Gpsdata/mock");
			File file2 = new File(Environment.getExternalStorageDirectory(), "/Gpsdata/record");
			if(file1.mkdirs()){
				Log.i(TAG, "/mock 创建成功");
			} else {
				Log.i(TAG, "/mock 创建失败");
			}
			if(file2.mkdirs()){
				Log.i(TAG, "/record 创建成功");
			} else {
				Log.i(TAG, "/record 创建失败");
			}
		} else {
			Log.i(TAG, "存在");
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 0,
				/* magic */5);
		
		@SuppressWarnings("deprecation")
		Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.app_name),
				System.currentTimeMillis());

		PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(this, GpsReplayActivity.class), 0);
		notification.setLatestEventInfo(this, "GPS回放模块", "Running...", pendingintent);
		startForeground(0x111, notification);
		
		return myBinder;
	}

	public class MyBinder extends Binder {
		public GpsReplayService getService1() {
			return GpsReplayService.this;
		}
	}
	
	public void startMockLocation() {
		handler.post(update_thread);
	}

	public void stopMockLocation() {
		handler.removeCallbacks(update_thread);
		mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
		mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
	}

	Runnable update_thread = new Runnable() {
		public void run() {
			LATITUDE = latSendList.get(count);
			LONGITUDE = lonSendList.get(count);
			setMockLocation(LocationManager.GPS_PROVIDER);
			setMockLocation(LocationManager.NETWORK_PROVIDER);
			handler.postDelayed(update_thread, UPDATE_TIME);
			count++;
			if(count > latSendList.size() && count > lonSendList.size()){
				handler.removeCallbacks(update_thread);
			}
		}
	};
	
	private void setMockLocation(String PROVIDER) {
		mLocationManager.addTestProvider(
				PROVIDER, "requiresNetwork" == "", "requiresSatellite" == "", "requiresCell" == "",
				"hasMonetaryCost" == "", "supportsAltitude" == "", "supportsSpeed" == "", "supportsBearing" == "",

				android.location.Criteria.POWER_LOW, android.location.Criteria.ACCURACY_FINE);

		Location newLocation = new Location(PROVIDER);
		newLocation.setLatitude(LATITUDE);
		newLocation.setLongitude(LONGITUDE);
		newLocation.setAltitude(500 + rad.nextFloat() * 50);
		newLocation.setAccuracy(50.f);
		newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
		newLocation.setTime(System.currentTimeMillis());
		mLocationManager.setTestProviderEnabled(PROVIDER, true);
		mLocationManager.setTestProviderStatus(PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		mLocationManager.setTestProviderLocation(PROVIDER, newLocation);
	}
}
