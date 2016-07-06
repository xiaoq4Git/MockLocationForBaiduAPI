package com.codoon.location.service;

import java.util.Random;

import com.codoon.baidulocationdemo.R;
import com.codoon.location.demo.MockActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

public class MockService extends Service {
	private Handler handler = new Handler();
	private MyBinder myBinder = new MyBinder();
	private LocationManager mLocationManager;
	private static double latitude = 0;
	private static double longitude = 0;
	private static double unit = 0;
	private static double paceValue = 0.000035;
	private static final int UPDATE_TIME = 1000;
	private Random rad;
	
	public void init() {
		rad = new Random();
		latitude = 30.5525326188;
		longitude = 104.0329972433;
	}
	
	public void setPaceValue(double paceValue) {
		MockService.paceValue = paceValue;
	}
	
	public void setLatitude(double latitude) {
		MockService.latitude = latitude;
	}

	public  void setLongitude(double longitude) {
		MockService.longitude = longitude;
	}

	@Override
	public IBinder onBind(Intent intent) {
		init();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mLocationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 0,
				/* magic */5);
		mLocationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
		
		Notification notification = new Notification(R.drawable.ic_launcher, getString(R.string.app_name),
				System.currentTimeMillis());

		PendingIntent pendingintent = PendingIntent.getActivity(this, 0, new Intent(this, MockActivity.class), 0);
		notification.setLatestEventInfo(this, "GPS模拟模块..", "正在运行...", pendingintent);
		startForeground(0x111, notification);
		
		return myBinder;
	}
	
	public class MyBinder extends Binder {
		public MockService getMockService() {
			return MockService.this;
		}
	}
	
	public void startMock() {
		handler.post(update_thread);
	}

	public void pauseMock() {
		handler.removeCallbacks(update_thread);
	}

	public void continueMock() {
		handler.post(update_thread);
	}

	public void stopMock() {
		handler.removeCallbacks(update_thread);
		mLocationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
		mLocationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
		init();
	}

	Runnable update_thread = new Runnable() {
		public void run() {
			unit += paceValue;
			if (unit > 1)
				unit = 0;
			setMockLocation(LocationManager.GPS_PROVIDER);
			setMockLocation(LocationManager.NETWORK_PROVIDER);
			handler.postDelayed(update_thread, UPDATE_TIME);
		}
	};
	
	private void setMockLocation(String PROVIDER) {
		mLocationManager.addTestProvider(
				PROVIDER, "requiresNetwork" == "", "requiresSatellite" == "", "requiresCell" == "",
				"hasMonetaryCost" == "", "supportsAltitude" == "", "supportsSpeed" == "", "supportsBearing" == "",

				android.location.Criteria.POWER_LOW, android.location.Criteria.ACCURACY_FINE);

		Location newLocation = new Location(PROVIDER);
		newLocation.setLatitude(latitude + unit);
		newLocation.setLongitude(longitude + unit);
		newLocation.setAltitude(500 + rad.nextFloat() * 50);
		newLocation.setAccuracy(50.f);
		newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
		newLocation.setTime(System.currentTimeMillis());
		mLocationManager.setTestProviderEnabled(PROVIDER, true);
		mLocationManager.setTestProviderStatus(PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		mLocationManager.setTestProviderLocation(PROVIDER, newLocation);
	}
}
