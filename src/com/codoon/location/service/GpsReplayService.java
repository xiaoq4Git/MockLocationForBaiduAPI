package com.codoon.location.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.codoon.baidulocationdemo.R;
import com.codoon.location.demo.GpsReplayActivity;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

public class GpsReplayService extends Service {
	public static double pi = 3.1415926535897932384626;
	public static double a = 6378245.0;
	public static double ee = 0.00669342162296594323;
	
	public static final String TAG = GpsReplayService.class.getName();
	private static final int UPDATE_TIME = 1800;
	private MyBinder myBinder = new MyBinder();
	private Handler handler = new Handler();
	private LocationManager mLocationManager;
	private static double LATITUDE = 0;
	private static double LONGITUDE = 0;
	private static double SEALEVEL = 0;
	private static List<Double> latSendList = new ArrayList<Double>();
	private static List<Double> lonSendList = new ArrayList<Double>();
	private static List<Double> highSendList = new ArrayList<Double>();
	private int count = 0;

	public void setHighSendList(List<Double> highList) {
		highSendList = highList;
	}

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
			SEALEVEL = highSendList.get(count);
			setMockLocation(LocationManager.GPS_PROVIDER);
			setMockLocation(LocationManager.NETWORK_PROVIDER);
			handler.postDelayed(update_thread, UPDATE_TIME);
			count++;
			if(count >= latSendList.size() && count >= lonSendList.size()){
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
//		newLocation.setLatitude(LATITUDE);
//		newLocation.setLongitude(LONGITUDE);
//		Gps position = bd09_To_Gcj02(LATITUDE, LONGITUDE);
		Gps position = gcj_To_Gps84(LATITUDE, LONGITUDE);
		newLocation.setLatitude(position.getWgLat());
		newLocation.setLongitude(position.getWgLon());
		newLocation.setAltitude(SEALEVEL);
		newLocation.setAccuracy(0.5f);
		newLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
		newLocation.setTime(System.currentTimeMillis());
		mLocationManager.setTestProviderEnabled(PROVIDER, true);
		mLocationManager.setTestProviderStatus(PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis());
		mLocationManager.setTestProviderLocation(PROVIDER, newLocation);
		Log.i("-->Latitude", "la:" + newLocation.getLatitude());
		Log.i("-->Longitude", "lo:" + newLocation.getLongitude());
	}
	
	public static Gps bd09_To_Gcj02(double bd_lat, double bd_lon) {
		double x = bd_lon - 0.0065, y = bd_lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
		double gg_lon = z * Math.cos(theta);
		double gg_lat = z * Math.sin(theta);
		return new Gps(gg_lat, gg_lon);
	}
	
	public static Gps gcj_To_Gps84(double lat, double lon) {
		Gps gps = transform(lat, lon);
		double lontitude = lon * 2 - gps.getWgLon();
		double latitude = lat * 2 - gps.getWgLat();
		return new Gps(latitude, lontitude);
	}
	
	public static boolean outOfChina(double lat, double lon) {
		if (lon < 72.004 || lon > 137.8347)
			return true;
		if (lat < 0.8293 || lat > 55.8271)
			return true;
		return false;
	}

	public static Gps transform(double lat, double lon) {
		if (outOfChina(lat, lon)) {
			return new Gps(lat, lon);
		}
		double dLat = transformLat(lon - 105.0, lat - 35.0);
		double dLon = transformLon(lon - 105.0, lat - 35.0);
		double radLat = lat / 180.0 * pi;
		double magic = Math.sin(radLat);
		magic = 1 - ee * magic * magic;
		double sqrtMagic = Math.sqrt(magic);
		dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
		dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
		double mgLat = lat + dLat;
		double mgLon = lon + dLon;
		return new Gps(mgLat, mgLon);
	}

	public static double transformLat(double x, double y) {
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
				+ 0.2 * Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
		ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
		return ret;
	}

	public static double transformLon(double x, double y) {
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
				* Math.sqrt(Math.abs(x));
		ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
		ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
		ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
				* pi)) * 2.0 / 3.0;
		return ret;
	}
}
